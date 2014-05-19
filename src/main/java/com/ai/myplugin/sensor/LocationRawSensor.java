/**
 * Created by User: veselin
 * On Date: 24/12/13
 */

package com.ai.myplugin.sensor;

import com.ai.api.SensorPlugin;
import com.ai.api.SensorResult;
import com.ai.api.SessionContext;
import com.ai.myplugin.util.APIKeys;
import com.ai.myplugin.util.Rest;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@PluginImplementation
public class LocationRawSensor implements SensorPlugin {
    protected static final Log log = LogFactory.getLog(LocationRawSensor.class);


    static final String LOCATION = "location";
    String location;
    String [] states = {"Collected", "Not Collected"};
    private static final String NAME = "LocationRawSensor";
    @Override
    public String[] getRequiredProperties() {
        return new String []{"Location"};
    }

    @Override
    public String[] getRuntimeProperties() {
        return new String[]{};
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
        return "Execute location sensor and provides raw data, return state is only indication whether the test eas successful";
    }

    @Override
    public SensorResult execute(SessionContext testSessionContext) {
        log.info("execute "+ getName() + ", sensor type:" +this.getClass().getName());

        try {
            final JSONObject jsonObject = getLongitudeLatitudeForAddress(location);
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
            log.error(e.getLocalizedMessage());
            e.printStackTrace();
            return new SensorResult() {
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

    public static JSONObject getLongitudeLatitudeForAddress(String address) throws Exception {
        Map<String, String> map = new ConcurrentHashMap<String, String>();
        map.put("X-Mashape-Authorization", APIKeys.getMashapeKey());

        String url = "https://montanaflynn-geocode-location-information.p.mashape.com/address?address=" + URLEncoder.encode(address);
        //String url = "https://metropolis-api-geocode.p.mashape.com/solve?address=" + URLEncoder.encode(address);
        String ret = Rest.httpGet(url, map);

        //curl "https://maps.googleapis.com/maps/api/geocode/json?address=1600+Amphitheatre+Parkway,+Mountain+View,+CA&sensor=false&key=AIzaSyAB4NA8aZi1wXgKRbMN8Z5BdNm7NkI9nb0"

        /*String url =  "https://maps.googleapis.com/maps/api/geocode/json?address="  + URLEncoder.encode(address) +
                "&sensor=false&key="+APIKeys.getGoogleKey(); */
        //String ret = Rest.httpGet(url);
        return (JSONObject) new JSONParser().parse(ret);
    }

    public static void main(String []args){
        LocationRawSensor locationRawSensor = new LocationRawSensor();
        locationRawSensor.setProperty(LocationRawSensor.LOCATION, "Gent, Mahy Lien Wondelgemstraat 160 ");
        System.out.println(locationRawSensor.execute(null).getRawData());
    }
}
