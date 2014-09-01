/**
 * Created by User: veselin
 * On Date: 24/12/13
 */

package com.ai.myplugin.sensor;

import com.ai.api.*;
import com.ai.myplugin.util.APIKeys;
import com.ai.myplugin.util.Geocoder;
import com.ai.myplugin.util.Rest;
import com.ai.myplugin.util.Utils;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@PluginImplementation
@PluginHeader(version = "1.0.1", author = "Veselin", category = "Location", iconURL = "http://app.waylay.io/icons/location.png")
public class LatitudeLongitudeRawSensor implements SensorPlugin {

    private static final Logger log = LoggerFactory.getLogger(LatitudeLongitudeRawSensor.class);

    static final String LATITUDE = "latitude";
    static final String LONGITUDE = "longitude";
    Double latitudeCoordinate;
    Double longitudeCoordinate;
    String [] states = {"Collected", "Not Collected"};
    private static final String NAME = "ReverseLookupAddress";

    @Override
    public Map<String, PropertyType> getRequiredProperties() {
        Map<String, PropertyType> map = new HashMap<>();
        map.put(LATITUDE, new PropertyType(DataType.DOUBLE, true, false));
        map.put(LONGITUDE, new PropertyType(DataType.DOUBLE, true, false));
        return map;
    }

    @Override
    public void setProperty(String string, Object obj) {
        if(string.equalsIgnoreCase(LATITUDE)) {
            latitudeCoordinate = Utils.getDouble(obj);
        } else if(string.equalsIgnoreCase(LONGITUDE)) {
            longitudeCoordinate =  Utils.getDouble(obj);
        }
    }

    @Override
    public Object getProperty(String string) {
        if(string.equalsIgnoreCase(LATITUDE)) {
            return latitudeCoordinate;
        } else if(string.equalsIgnoreCase(LONGITUDE)) {
            return longitudeCoordinate;
        } else {
            throw new RuntimeException("Property "+ string + " not in the required settings");
        }
    }

    @Override
    public String getDescription() {
        return "Execute location sensor and provides raw data, return state is only indication whether the test eas successful";
    }

    @Override
    public SensorResult execute(SessionContext testSessionContext) {
        log.info("execute "+ getName() + ", sensor type:" +this.getClass().getName());

        try {
            final JSONObject locationObject = Geocoder.reverseLookupAddress(longitudeCoordinate, latitudeCoordinate);
            log.info(locationObject.toJSONString());
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
                    return locationObject.toJSONString();
                }
            };
        } catch (Exception e) {
            log.error(e.getLocalizedMessage(),e );
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

}
