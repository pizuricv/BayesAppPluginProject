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
import com.ai.myplugin.util.Utils;
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

@PluginImplementation
public class LatitudeLongitudeRawSensor implements SensorPlugin {
    protected static final Log log = LogFactory.getLog(LocationRawSensor.class);

    static final String LATITUDE = "latitude";
    static final String LONGITUDE = "longitude";
    Double latitudeCoordinate;
    Double longitudeCoordinate;
    String [] states = {"Collected", "Not Collected"};
    private static final String NAME = "LatitudeLongitudeRawSensor";
    @Override
    public String[] getRequiredProperties() {
        return new String []{"latitude", "longitude"};
    }

    @Override
    public String[] getRuntimeProperties() {
        return new String[]{};
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
            final JSONObject locationObject = reverseLookupAddress(longitudeCoordinate, latitudeCoordinate);
            log.info(locationObject.toJSONString());
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
                    return locationObject.toJSONString();
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

    public static JSONObject reverseLookupAddress(double longitude, double latitude) throws Exception {
        Map<String, String> map = new ConcurrentHashMap<String, String>();
        map.put("X-Mashape-Authorization", APIKeys.getMashapeKey());
        String latitudeCoordinateStr = LATITUDE + "="+ URLEncoder.encode(Double.toString(latitude));
        String longitudeCoordinateStr = LONGITUDE + "="+ URLEncoder.encode(Double.toString(longitude));
        String url = "https://montanaflynn-geocode-location-information.p.mashape.com/reverse?" + longitudeCoordinateStr + "&"+ latitudeCoordinateStr;
        String ret = Rest.httpsGet(url, map);
        return (JSONObject) new JSONParser().parse(ret);

       /* Google
       String url = "https://maps.googleapis.com/maps/api/geocode/json?latlng="+ latitude +","+longitude+
                "&sensor=false&key=" + APIKeys.getGoogleKey();
        String ret = Rest.httpsGet(url);
        //return (JSONObject) new JSONParser().parse(ret);
        //TODO hack. need better parsing later
        JSONObject obj = (JSONObject) new JSONParser().parse(ret);
        JSONArray array = (JSONArray) obj.get("results");
        JSONObject object = (JSONObject) array.get(0);
        String addr = (String) object.get("formatted_address");
        StringTokenizer stringTokenizer = new StringTokenizer(addr, ",");
        String street = stringTokenizer.nextToken().trim();
        String city = stringTokenizer.nextToken().trim();
        String country = stringTokenizer.nextToken().trim();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("street", street);
        jsonObject.put("city", city);
        jsonObject.put("country", country);
        return jsonObject;               */

    }

    public static void main(String []args) throws ParseException {
        LatitudeLongitudeRawSensor locationSensor = new LatitudeLongitudeRawSensor();
        locationSensor.setProperty(LONGITUDE, 19.851858);
        locationSensor.setProperty(LATITUDE, 45.262231);
        System.out.println(locationSensor.execute(null).getRawData());
        System.out.println(((JSONObject)new JSONParser().parse(locationSensor.execute(null).getRawData())).get("city"));
    }
}
