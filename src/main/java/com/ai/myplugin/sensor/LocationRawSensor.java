/**
 * Created by User: veselin
 * On Date: 24/12/13
 */

package com.ai.myplugin.sensor;

import com.ai.api.*;
import com.ai.myplugin.util.Geocoder;
import com.ai.myplugin.util.LatLng;
import net.xeoh.plugins.base.annotations.PluginImplementation;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@PluginImplementation
@PluginHeader(version = "1.0.1", author = "Veselin", category = "Location", iconURL = "http://app.waylay.io/icons/location.png")
public class LocationRawSensor implements SensorPlugin {

    private static final Logger log = LoggerFactory.getLogger(LocationRawSensor.class);


    static final String LOCATION = "location";
    String location;
    String [] states = {"Collected", "Not Collected"};
    private static final String NAME = "LongitudeLatitude";

    @Override
    public Map<String, PropertyType> getRequiredProperties() {
        Map<String, PropertyType> map = new HashMap<>();
        map.put(LOCATION, new PropertyType(DataType.STRING, true, false));
        return map;
    }

    @Override
    public void setProperty(String string, Object obj) {
        if(string.equalsIgnoreCase(LOCATION)) {
            location = URLEncoder.encode((String) obj);
        } else {
            throw new RuntimeException("Property "+ string + " not in the required settings");
        }
    }

    @Override
    public Object getProperty(String s) {
        return location;
    }

    @Override
    public String getDescription() {
        return "Location sensor that returns longitude and latitude in raw data, " +
                "return state only indicates whether the test eas successful";
    }

    @Override
    public SensorResult execute(SessionContext testSessionContext) {
        log.info("execute "+ getName() + ", sensor type:" +this.getClass().getName());

        try {
            LatLng latLng = Geocoder.getLongitudeLatitudeForAddress(location);
            final JSONObject jsonObject = new JSONObject();
            jsonObject.put("latitude", latLng.latitude);
            jsonObject.put("longitude", latLng.longitude);
            return new SensorResult() {
                @Override
                public boolean isSuccess() {
                    return true;
                }

                @Override
                public String getObserverState() {
                    return states[0];
                }

                @Override
                public List<Map<String, Number>> getObserverStates() {
                    return null;
                }

                @Override
                public String getRawData() {
                    return jsonObject.toJSONString();
                }
            };
        } catch (Exception e) {
            log.error(e.getLocalizedMessage(), e);
            return new SensorResult() {
                @Override
                public boolean isSuccess() {
                    return true; //TODO need better way to provide BN with RAW SENSORS!!
                }

                @Override
                public String getObserverState() {
                    return states[1];
                }

                @Override
                public List<Map<String, Number>> getObserverStates() {
                    return null;
                }

                @Override
                public String getRawData() {
                    return null;
                }
            };
        }
    }

    @Override
    public Map<String, RawDataType> getProducedRawData() {
        Map<String, RawDataType> map = new ConcurrentHashMap<>();
        map.put("zip", new RawDataType("string", DataType.STRING, true, CollectedType.INSTANT));
        map.put("country", new RawDataType("string", DataType.STRING, true, CollectedType.INSTANT));
        map.put("city", new RawDataType("string", DataType.STRING, true, CollectedType.INSTANT));
        map.put("latitude", new RawDataType("deg", DataType.DOUBLE, true, CollectedType.INSTANT));
        map.put("longitude", new RawDataType("deg", DataType.DOUBLE, true, CollectedType.INSTANT));
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

    public static void main(String []args){
        LocationRawSensor locationRawSensor = new LocationRawSensor();
        locationRawSensor.setProperty(LocationRawSensor.LOCATION, "Gent, Mahy Lien Wondelgemstraat 160 ");
        System.out.println(locationRawSensor.execute(null).getRawData());
    }
}
