/**
 * Created by User: veselin
 * On Date: 26/12/13
 */

package com.ai.myplugin.sensor;

import com.ai.api.*;
import com.ai.myplugin.util.*;
import net.xeoh.plugins.base.annotations.PluginImplementation;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


@PluginImplementation
@PluginHeader(version = "1.0.1", author = "Veselin", category = "Location", iconURL = "http://app.waylay.io/icons/location.png")
public class LocationSensor implements SensorPlugin {

    private static final Logger log = LoggerFactory.getLogger(LocationSensor.class);

    static final String LOCATION = "location";
    static final String LATITUDE = "latitude";
    static final String LONGITUDE = "longitude";
    static final String DISTANCE = "distance";

    static final String RUNTIME_LATITUDE = "latitude";
    static final String RUNTIME_LONGITUDE = "longitude";

    private static final String NAME = "LocationWithinDistance";

    private final Map<String, Object> propertiesMap = new ConcurrentHashMap<>();

    private final String [] states = {"Within", "Out"};

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
    public Map<String, RawDataType> getRequiredRawData() {
        Map<String, RawDataType> map = new HashMap<>();
        map.put(RUNTIME_LATITUDE, new RawDataType("deg", DataType.DOUBLE));
        map.put(RUNTIME_LONGITUDE, new RawDataType("deg", DataType.DOUBLE));
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
        return "Checks whether current location (from runtime data) is within a distance from a given location";
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
            return SensorResultBuilder.failure().build();
        }
        Double runtime_latitude = Utils.getDouble(rt1);
        Double runtime_longitude = Utils.getDouble(rt2);
        log.info("Current location: "+ runtime_latitude + ","+runtime_longitude);

        Double configuredLatitude = getProperty(LATITUDE) == null || "".equals(getProperty(LATITUDE))?
                Double.MAX_VALUE: Utils.getDouble(getProperty(LATITUDE));
        Double configuredLongitude = getProperty(LONGITUDE) == null || "".equals(getProperty(LONGITUDE))?
                Double.MAX_VALUE: Utils.getDouble(getProperty(LONGITUDE));


        Map currentData = new ConcurrentHashMap();
        try {
            log.info("try to get more from runtime data");
            JSONObject jsonObjectRuntime = Geocoder.reverseLookupAddress(runtime_longitude, runtime_latitude) ;
            String city = jsonObjectRuntime.get("city") == null ? "not found" : jsonObjectRuntime.get("city").toString();
            String country = jsonObjectRuntime.get("country") == null ? "not found" : jsonObjectRuntime.get("country").toString();
            String streetName = jsonObjectRuntime.get("street_name") == null ? "not found" : jsonObjectRuntime.get("street_name").toString();
            String number = jsonObjectRuntime.get("street_number") == null ? "not found " : jsonObjectRuntime.get("street_number").toString();
            currentData.put("current_city", city);
            currentData.put("current_country", country);
            currentData.put("current_street", streetName);
            currentData.put("current_street_number", number);
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }


        JSONObject rawData = new JSONObject();

        if(!configuredLatitude.equals(Double.MAX_VALUE) && !configuredLongitude.equals(Double.MAX_VALUE)){
            log.info("Location configured, try to get more data");
            try {
                rawData = Geocoder.reverseLookupAddress(configuredLongitude, configuredLatitude);
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
            }
        } else {
            try {
                if(getProperty(LOCATION) != null){
                    log.info("Location configured as the address: " + getProperty(LOCATION) +  " , try to get coordinates");
                    LatLng latLng = Geocoder.getLongitudeLatitudeForAddress(getProperty(LOCATION).toString());
                    configuredLongitude = latLng.longitude;
                    configuredLatitude = latLng.latitude;
                    rawData.put("configured_latitude", configuredLatitude);
                    rawData.put("configured_longitude", configuredLongitude);
                } else {
                    throw new RuntimeException("configured location not properly set");
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                return SensorResultBuilder.failure().build();
            }
        }
        if(currentData.size() > 0){
           rawData.putAll(currentData);
        }
        log.info("Configured location: "+ configuredLatitude + ","+configuredLongitude);
        double distance = FormulaParser.calculateDistance(runtime_latitude, runtime_longitude,
                configuredLatitude, configuredLongitude);
        log.info("Computed distance: " + distance);
        rawData.put("distance", distance);
        rawData.put(RUNTIME_LATITUDE, runtime_latitude);
        rawData.put(RUNTIME_LONGITUDE, runtime_longitude);

        log.info("raw data is " + rawData.toJSONString());

        final String state;
        if(distance  < Utils.getDouble(getProperty(DISTANCE)))
            state = states[0];
        else
            state = states[1];

        final JSONObject finalRawData = rawData;
        return new SensorResult() {
            @Override
            public boolean isSuccess() {
                return true;
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
                return finalRawData.toJSONString();
            }
        };

    }

    @Override
    public Map<String, RawDataType> getProducedRawData() {
        Map<String, RawDataType> map = new ConcurrentHashMap<>();
        map.put(RUNTIME_LATITUDE, new RawDataType("location", DataType.DOUBLE, true, CollectedType.INSTANT));
        map.put(RUNTIME_LONGITUDE, new RawDataType("location", DataType.DOUBLE, true, CollectedType.INSTANT));
        map.put("distance", new RawDataType("double", DataType.DOUBLE, true, CollectedType.COMPUTED));
        map.put("zip", new RawDataType("string", DataType.STRING, true, CollectedType.INSTANT));
        map.put("country", new RawDataType("string", DataType.STRING, true, CollectedType.INSTANT));
        map.put("city", new RawDataType("string", DataType.STRING, true, CollectedType.INSTANT));
        map.put("latitude", new RawDataType("location", DataType.DOUBLE, true, CollectedType.INSTANT));
        map.put("latitude", new RawDataType("location", DataType.DOUBLE, true, CollectedType.INSTANT));
        map.put("longitude", new RawDataType("location", DataType.DOUBLE, true, CollectedType.INSTANT));
        map.put("street_number", new RawDataType("number", DataType.INTEGER, true, CollectedType.INSTANT));
        map.put("region", new RawDataType("string", DataType.STRING, true, CollectedType.INSTANT));
        map.put("street_name", new RawDataType("string", DataType.STRING, true, CollectedType.INSTANT));
        return map;
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
    public Set<String> getSupportedStates() {
        return new HashSet(Arrays.asList(states));
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
