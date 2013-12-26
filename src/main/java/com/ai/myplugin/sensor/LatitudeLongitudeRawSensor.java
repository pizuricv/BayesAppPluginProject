package com.ai.myplugin.sensor;

import com.ai.bayes.plugins.BNSensorPlugin;
import com.ai.bayes.scenario.TestResult;
import com.ai.myplugin.util.Mashape;
import com.ai.myplugin.util.Rest;
import com.ai.myplugin.util.Utils;
import com.ai.util.resource.TestSessionContext;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by User: veselin
 * On Date: 24/12/13
 */
@PluginImplementation
public class LatitudeLongitudeRawSensor implements BNSensorPlugin {
    protected static final Log log = LogFactory.getLog(LocationRawSensor.class);

    static final String server = "https://montanaflynn-geocode-location-information.p.mashape.com/reverse?";
    static final String LATITUDE = "latitude";
    static final String LONGITUDE = "longitude";
    String latitudeCoordinate;
    String longitudeCoordinate;
    String [] states = {"Collected", "Not Collected"};
    private static final String NAME = "LatitudeLongitudeRawSensor";
    @Override
    public String[] getRequiredProperties() {
        return new String []{"latitude", "longitude"};
    }

    @Override
    public void setProperty(String string, Object obj) {
        if(string.equalsIgnoreCase(LATITUDE)) {
            latitudeCoordinate = LATITUDE + "="+ URLEncoder.encode(Utils.getDouble(obj).toString());
        } else if(string.equalsIgnoreCase(LONGITUDE)) {
            longitudeCoordinate = LONGITUDE + "="+ URLEncoder.encode(Utils.getDouble(obj).toString());
        } else {
            throw new RuntimeException("Property "+ string + " not in the required settings");
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
    public TestResult execute(TestSessionContext testSessionContext) {
        log.info("execute "+ getName() + ", sensor type:" +this.getClass().getName());
        Map<String, String> map = new ConcurrentHashMap<String, String>();
        map.put("X-Mashape-Authorization", Mashape.getKey());
        try {
            final String str = Rest.httpGet(server + longitudeCoordinate + "&"+ latitudeCoordinate, map);
            log.info(str);
            return new TestResult() {
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
                    return states[0];
                }

                @Override
                public List<Map<String, Number>> getObserverStates() {
                    return null;
                }

                @Override
                public String getRawData() {
                    return str;
                }
            };
        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
            e.printStackTrace();
            return new TestResult() {
                @Override
                public boolean isSuccess() {
                    return true; //TODO need better way to provide BN with RAW SENSORS!!
                }

                @Override
                public String getName() {
                    return "Location result";
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
    public void shutdown(TestSessionContext testSessionContext) {

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
        LatitudeLongitudeRawSensor locationSensor = new LatitudeLongitudeRawSensor();
        locationSensor.setProperty(LONGITUDE, 19.851858);
        locationSensor.setProperty(LATITUDE, 45.262231);
        System.out.println(locationSensor.execute(null).getRawData());
        System.out.println(((JSONObject)new JSONParser().parse(locationSensor.execute(null).getRawData())).get("city"));
    }
}
