/**
 * Created by User: veselin
 * On Date: 21/03/14
 */

package com.ai.myplugin.sensor;

import com.ai.api.SensorPlugin;
import com.ai.api.SensorResult;
import com.ai.api.SessionContext;
import com.ai.myplugin.util.APIKeys;
import com.ai.myplugin.util.EmptyTestResult;
import com.ai.myplugin.util.Rest;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.util.List;
import java.util.Map;


@PluginImplementation
public class SensulosSensor implements SensorPlugin {

    private static final Log log = LogFactory.getLog(SensulosSensor.class);
    String baseUrl = "http://in.sensolus.com:8080";
    String user = "apps4ghent";
    String [] states = {"Collected", "Not Collected"};
    private static final String NAME = "SensulosSensor";
    private static final String ID = "ID";
    private String id = "";


    @Override
    public SensorResult execute(SessionContext testSessionContext) {
        //String url = baseUrl + "/server/rest/connectednodes?owner_id=" + user + "&token=" + APIKeys.getSensulosKey();
        String url = baseUrl + "/server/rest/connectednodes/" + id + "/data/lastvalues?token=" + APIKeys.getSensulosKey();
        try {
            String stringToParse = Rest.httpGet(url);
            log.info(stringToParse);
            final JSONObject jsonObject = (JSONObject) new JSONParser().parse(stringToParse);
            return new SensorResult() {
                @Override
                public boolean isSuccess() {
                    return true;
                }

                @Override
                public String getName() {
                    return "Sensulos raw data";
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
                    //why on Earth?  well content is non-parsable string... back to JSON...
                    //"content": "{\"qualitative_distance\":\"medium\",\"rssi\":-75,\"router_id\":\"apps4ghent-gateway-fixed-sensors-2\"}",
                    JSONObject rawData = new JSONObject();
                    for (Object key1 : jsonObject.keySet()){
                        String key = key1.toString();
                        try {
                            if("router_distance".equalsIgnoreCase(key)){
                                JSONObject jsonObject1 = (JSONObject) jsonObject.get(key);
                                JSONObject jsonObject2 = (JSONObject) new JSONParser().parse(jsonObject1.get("content").toString());
                                rawData.put("qualitative_distance", jsonObject2.get("qualitative_distance"));
                                rawData.put("rssi", jsonObject2.get("rssi"));
                                rawData.put("router_id", jsonObject2.get("router_id"));
                            } else if("humidity".equalsIgnoreCase(key)){
                                JSONObject jsonObject1 = (JSONObject) jsonObject.get(key);
                                JSONObject jsonObject2 = (JSONObject) new JSONParser().parse(jsonObject1.get("content").toString());
                                rawData.put("humidity", jsonObject2.get("value"));

                            } else if("pressure".equalsIgnoreCase(key)){
                                JSONObject jsonObject1 = (JSONObject) jsonObject.get(key);
                                JSONObject jsonObject2 = (JSONObject) new JSONParser().parse(jsonObject1.get("content").toString());
                                rawData.put("pressure", jsonObject2.get("value"));
                            } else if("ir_temperature".equalsIgnoreCase(key)){
                                JSONObject jsonObject1 = (JSONObject) jsonObject.get(key);
                                JSONObject jsonObject2 = (JSONObject) new JSONParser().parse(jsonObject1.get("content").toString());
                                rawData.put("ambient_temperature", jsonObject2.get("ambient_temperature"));
                                rawData.put("object_temperature", jsonObject2.get("object_temperature"));
                            } else if("magnetic_field_strength".equalsIgnoreCase(key)){

                            } else if("battery".equalsIgnoreCase(key)){
                                JSONObject jsonObject1 = (JSONObject) jsonObject.get(key);
                                JSONObject jsonObject2 = (JSONObject) new JSONParser().parse(jsonObject1.get("content").toString());
                                rawData.put("battery_remaining", jsonObject2.get("remaining"));

                            } else if("acceleration".equalsIgnoreCase(key)){
                                JSONObject jsonObject1 = (JSONObject) jsonObject.get(key);
                                JSONObject jsonObject2 = (JSONObject) new JSONParser().parse(jsonObject1.get("content").toString());
                                rawData.put("acceleration_accX", jsonObject2.get("accX"));
                                rawData.put("acceleration_accY", jsonObject2.get("accY"));
                                rawData.put("acceleration_accZ", jsonObject2.get("accZ"));
                            } else if("door_status".equalsIgnoreCase(key)){
                                JSONObject jsonObject1 = (JSONObject) jsonObject.get(key);
                                JSONObject jsonObject2 = (JSONObject) new JSONParser().parse(jsonObject1.get("content").toString());
                                rawData.put("door_status_open", jsonObject2.get("open"));

                            } else if("door_angle".equalsIgnoreCase(key)){
                                JSONObject jsonObject1 = (JSONObject) jsonObject.get(key);
                                JSONObject jsonObject2 = (JSONObject) new JSONParser().parse(jsonObject1.get("content").toString());
                                rawData.put("door_angle_x", jsonObject2.get("x"));
                                rawData.put("door_angle_y", jsonObject2.get("y"));
                                rawData.put("door_angle_z", jsonObject2.get("z"));
                                rawData.put("door_angle_dX", jsonObject2.get("dX"));
                                rawData.put("door_angle_dY", jsonObject2.get("dY"));
                                rawData.put("door_angle_dZ", jsonObject2.get("dZ"));
                                rawData.put("door_angle_total_delta", jsonObject2.get("total delta"));
                            } else if("magnetic_field_strength".equalsIgnoreCase(key)){
                                JSONObject jsonObject1 = (JSONObject) jsonObject.get(key);
                                JSONObject jsonObject2 = (JSONObject) new JSONParser().parse(jsonObject1.get("content").toString());
                                rawData.put("magneticX", jsonObject2.get("magneticX"));
                                rawData.put("magneticY", jsonObject2.get("magneticY"));
                                rawData.put("magneticZ", jsonObject2.get("magneticZ"));

                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            log.error(e.getMessage());
                        }

                    }
                    //return jsonObject.toString();
                    return rawData.toJSONString();
                }
            };
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            return new EmptyTestResult();
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

    @Override
    public String[] getRequiredProperties() {
        return new String[]{ID};
    }

    @Override
    public String[] getRuntimeProperties() {
        return new String[]{};
    }

    @Override
    public void setProperty(String s, Object o) {
        if(ID.equalsIgnoreCase(s)){
            id = o.toString();
        }

    }

    @Override
    public Object getProperty(String s) {
        if(ID.equalsIgnoreCase(s)){
            return id;
        }
        throw new RuntimeException("property "+s + " not known by the sensor");
    }

    @Override
    public String getDescription() {
        return "Sensulos sensor";
    }

    public static void main(String []args) {
        SensulosSensor sensulosSensor = new SensulosSensor();
        sensulosSensor.setProperty(ID, "1071364b-83c4-4491-aad1-f35faaba1e63");
        SessionContext testSessionContext = new SessionContext(1);

        SensorResult testResult = sensulosSensor.execute(testSessionContext);
        log.info(testResult.getRawData());
        log.info(testResult.getObserverState());

        sensulosSensor.setProperty(ID, "120ceb4a-92f2-4837-8ea4-4372cd559639");
        testResult = sensulosSensor.execute(testSessionContext);
        log.info(testResult.getRawData());
        log.info(testResult.getObserverState());

        sensulosSensor.setProperty(ID, "31a1967c-5a49-465c-a59d-7ef08f368b61");
        testResult = sensulosSensor.execute(testSessionContext);
        log.info(testResult.getRawData());
        log.info(testResult.getObserverState());


        sensulosSensor.setProperty(ID, "542bcd19-9ff2-4f84-a84d-7568d277ba5e");
        testResult = sensulosSensor.execute(testSessionContext);
        log.info(testResult.getRawData());
        log.info(testResult.getObserverState());


        sensulosSensor.setProperty(ID, "614ba218-913e-42a1-b4e8-af6fb232f2fb");

        testResult = sensulosSensor.execute(testSessionContext);
        log.info(testResult.getRawData());
        log.info(testResult.getObserverState());


        sensulosSensor.setProperty(ID, "8bb4ad28-fb61-492d-aaad-d0e544bf7f26");

        testResult = sensulosSensor.execute(testSessionContext);
        log.info(testResult.getRawData());
        log.info(testResult.getObserverState());

    }
}
