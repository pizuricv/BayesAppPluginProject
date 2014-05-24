/**
 * Created by User: veselin
 * On Date: 26/12/13
 */

package com.ai.myplugin.sensor;

import com.ai.api.*;
import com.ai.myplugin.util.*;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


@PluginImplementation
public class LocationSensor implements SensorPlugin {
    protected static final Log log = LogFactory.getLog(LocationSensor.class);
    static final String LOCATION = "location";
    static final String LATITUDE = "latitude";
    static final String LONGITUDE = "longitude";
    static final String DISTANCE = "distance";
    static final String RUNTIME_LATITUDE = "runtime_latitude";
    static final String RUNTIME_LONGITUDE = "runtime_longitude";

    Map<String, Object> propertiesMap = new ConcurrentHashMap<String, Object>();

    String [] states = {"Within", "Out"};
    private static final String NAME = "LocationSensor";

    @Override
    public Map<String, PropertyType> getRequiredProperties() {
        Map<String, PropertyType> map = new HashMap<>();
        map.put(LOCATION, new PropertyType(DataType.STRING, true, false));
        map.put(LATITUDE, new PropertyType(DataType.DOUBLE, true, false));
        map.put(LONGITUDE, new PropertyType(DataType.DOUBLE, true, false));
        map.put(DISTANCE, new PropertyType(DataType.DOUBLE, true, false));
        return map;
    }

    @Override
    public Map<String, PropertyType> getRuntimeProperties() {
        Map<String, PropertyType> map = new HashMap<>();
        map.put(RUNTIME_LATITUDE, new PropertyType(DataType.STRING, true, false));
        map.put(RUNTIME_LONGITUDE, new PropertyType(DataType.DOUBLE, true, false));
        return map;
    }

    @Override
    public void setProperty(String string, Object obj) {
        if(getRequiredProperties().keySet().contains(string)) {
            propertiesMap.put(string, obj);
        } else {
            throw new RuntimeException("Property "+ string + " not in the required settings");
        }
    }

    @Override
    public Object getProperty(String string) {
        return propertiesMap.get(string);
    }

    @Override
    public String getDescription() {
        return "Checks whether a collected data is within a distance from a given location";
    }

    @Override
    public SensorResult execute(SessionContext testSessionContext) {
        log.info("execute "+ getName() + ", sensor type:" +this.getClass().getName());
        if(getProperty(DISTANCE) == null)
            throw new RuntimeException("distance not set");

        Object rt1 = testSessionContext.getAttribute(RUNTIME_LATITUDE);
        Object rt2 = testSessionContext.getAttribute(RUNTIME_LONGITUDE);
        if(rt1 == null || rt2 == null){
            log.warn("no runtime longitude or latitude given");
            return new EmptySensorResult();
        }
        Double runtime_latitude = Utils.getDouble(rt1);
        Double runtime_longitude = Utils.getDouble(rt2);
        log.info("Current location: "+ runtime_latitude + ","+runtime_longitude);

        JSONObject jsonObject = new JSONObject();

        Map<String, String> map = new ConcurrentHashMap<String, String>();
        map.put("X-APIKeys-Authorization", APIKeys.getMashapeKey());

        Double configuredLatitude = getProperty(LATITUDE) == null || "".equals(getProperty(LATITUDE))?
                Double.MAX_VALUE: Utils.getDouble(getProperty(LATITUDE));
        Double configuredLongitude = getProperty(LONGITUDE) == null || "".equals(getProperty(LONGITUDE))?
                Double.MAX_VALUE: Utils.getDouble(getProperty(LONGITUDE));

        String str;

        Map currentData = new ConcurrentHashMap();
        try {
            log.info("try to get more from runtime data");
            JSONObject jsonObjectRuntime = LatitudeLongitudeRawSensor.reverseLookupAddress(runtime_longitude, runtime_latitude) ;
            String city = jsonObjectRuntime.get("city") == null ? "not found" : jsonObjectRuntime.get("city").toString();
            String country = jsonObjectRuntime.get("country") == null ? "not found" : jsonObjectRuntime.get("country").toString();
            String streetName = jsonObjectRuntime.get("street_name") == null ? "not found" : jsonObjectRuntime.get("street_name").toString();
            String number = jsonObjectRuntime.get("street_number") == null ? "not found " : jsonObjectRuntime.get("street_number").toString();
            currentData.put("current_city", city);
            currentData.put("current_country", country);
            currentData.put("current_street", streetName);
            currentData.put("current_street_number", number);
        } catch (Exception e) {
            e.printStackTrace();
            log.warn(e.getMessage());
        }

        if(!configuredLatitude.equals(Double.MAX_VALUE) && !configuredLongitude.equals(Double.MAX_VALUE)){
            log.info("Location configured, try to get more data");
            try {
                jsonObject = LatitudeLongitudeRawSensor.reverseLookupAddress(configuredLongitude, configuredLatitude);
            } catch (Exception e) {
                e.printStackTrace();
                log.warn(e.getMessage());
            }
        } else {
            try {
                if(getProperty(LOCATION) != null){
                    log.info("Location configured as the address: " + getProperty(LOCATION) +  " , try to get coordinates");
                    jsonObject = LocationRawSensor.getLongitudeLatitudeForAddress(getProperty(LOCATION).toString());
                    configuredLongitude = Utils.getDouble(jsonObject.get("longitude"));
                    configuredLatitude = Utils.getDouble(jsonObject.get("latitude"));
                    jsonObject.put("configured_latitude", configuredLatitude);
                    jsonObject.put("configured_longitude", configuredLongitude);
                } else
                    throw new RuntimeException("configured location not properly set");
            } catch (Exception e) {
                e.printStackTrace();
                log.error(e.getMessage());
                return new EmptySensorResult();
            }
        }
        if(currentData.size() > 0){
           jsonObject.putAll(currentData);
        }
        log.info("Configured location: "+ configuredLatitude + ","+configuredLongitude);
        double distance = FormulaParser.calculateDistance(runtime_latitude, runtime_longitude,
                configuredLatitude, configuredLongitude);
        log.info("Computed distance: " + distance);
        jsonObject.put("distance", distance);
        jsonObject.put(RUNTIME_LATITUDE, runtime_latitude);
        jsonObject.put(RUNTIME_LONGITUDE, runtime_longitude);

        log.info("raw data is "+jsonObject.toJSONString());

        final String state;
        if(distance  < Utils.getDouble(getProperty(DISTANCE)))
            state = states[0];
        else
            state = states[1];

        final JSONObject finalJsonObject = jsonObject;
        return new SensorResult() {
            @Override
            public boolean isSuccess() {
                return true;
            }

            @Override
            public String getName() {
                return "Location result";
            }

            @Override
            public String getObserverState() {
                return state;
            }

            @Override
            public List<Map<String, Number>> getObserverStates() {
                return null;
            }

            @Override
            public String getRawData() {
                return finalJsonObject.toJSONString();
            }
        };

    }

    @Override
    public List<RawDataType> getRawDataTypes() {
        List<RawDataType> list = new ArrayList<>();
        list.add(new RawDataType(RUNTIME_LATITUDE, "location", DataType.DOUBLE, true, CollectedType.INSTANT));
        list.add(new RawDataType(RUNTIME_LONGITUDE, "location", DataType.DOUBLE, true, CollectedType.INSTANT));
        list.add(new RawDataType("distance", "double", DataType.DOUBLE, true, CollectedType.COMPUTED));
        list.add(new RawDataType("zip", "string", DataType.STRING, true, CollectedType.INSTANT));
        list.add(new RawDataType("country", "string", DataType.STRING, true, CollectedType.INSTANT));
        list.add(new RawDataType("city", "string", DataType.STRING, true, CollectedType.INSTANT));
        list.add(new RawDataType("latitude", "location", DataType.DOUBLE, true, CollectedType.INSTANT));
        list.add(new RawDataType("latitude", "location", DataType.DOUBLE, true, CollectedType.INSTANT));
        list.add(new RawDataType("longitude", "location", DataType.DOUBLE, true, CollectedType.INSTANT));
        list.add(new RawDataType("street_number", "number", DataType.INTEGER, true, CollectedType.INSTANT));
        list.add(new RawDataType("region", "string", DataType.STRING, true, CollectedType.INSTANT));
        list.add(new RawDataType("street_name", "string", DataType.STRING, true, CollectedType.INSTANT));
        return list;
    }

    @Override
    public void setup(SessionContext testSessionContext) {
        log.debug("Setup : " + getName() + ", sensor : "+this.getClass().getName());
    }

    @Override
    public void shutdown(SessionContext testSessionContext) {

    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String[] getSupportedStates() {
        return states;
    }

    public static void main(String []args) throws ParseException {
        LocationSensor locationSensor = new LocationSensor();
        locationSensor.setProperty(LONGITUDE, 19.851858);
        locationSensor.setProperty(LATITUDE, 45.262231);
        locationSensor.setProperty(DISTANCE, 100);
        SessionContext testSessionContext = new SessionContext(1);
        testSessionContext.setAttribute(RUNTIME_LONGITUDE, 19.851858);
        testSessionContext.setAttribute(RUNTIME_LATITUDE, 45.262231);
        SensorResult testResult = locationSensor.execute(testSessionContext);
        System.out.println(testResult.getObserverState());
        System.out.println(testResult.getRawData());
    }
}
