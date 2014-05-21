/**
 * User: pizuricv
 * Date: 11/26/12
 */

package com.ai.myplugin.action;

import com.ai.api.*;
import com.ai.myplugin.util.Utils;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@PluginImplementation
public class ScenarioFactoryAction implements ActuatorPlugin{
    private static final Log log = LogFactory.getLog(ScenarioFactoryAction.class);
    private static final String SERVER_ADDRESS = "server address";
    private static final String USER_NAME = "username";
    private static final String USER_PASSWORD = "password";
    private static final String NETWORK = "network";
    private static final String TYPE = "type";
    private static final String TARGET = "target";
    private static final String STOP_STATE = "stop state";
    private static final String OPERATOR = "operator";
    private static final String THRESHOLD = "threshold";
    private static final String SCENARIO_NAME = "scenario name";
    private static final String RESOURCE = "resource name";
    private static final String START = "start";
    private static final String FREQUENCY = "frequency";
    private static final String NAME = "ScenarioFactory";


    private URL url;
    Map<String, Object> propertiesMap = new ConcurrentHashMap<String, java.lang.Object>();


    @Override
    public Map<String,PropertyType> getRequiredProperties() {
        Map<String,PropertyType> map = new HashMap<>();
        map.put(TARGET, new PropertyType(DataType.STRING, true, true));
        map.put(SERVER_ADDRESS, new PropertyType(DataType.STRING, true, true));
        map.put(USER_NAME, new PropertyType(DataType.STRING, true, true));
        map.put(USER_PASSWORD, new PropertyType(DataType.STRING, true, true));
        map.put(NETWORK, new PropertyType(DataType.STRING, true, true));
        map.put(TYPE, new PropertyType(DataType.STRING, true, true));
        map.put(FREQUENCY, new PropertyType(DataType.INTEGER, true, true));
        map.put(STOP_STATE, new PropertyType(DataType.STRING, true, true));
        map.put(OPERATOR, new PropertyType(DataType.INTEGER, true, true));
        map.put(THRESHOLD, new PropertyType(DataType.DOUBLE, true, true));
        map.put(SCENARIO_NAME, new PropertyType(DataType.STRING, true, true));
        map.put(RESOURCE, new PropertyType(DataType.STRING, true, true));
        map.put(START, new PropertyType(DataType.BOOLEAN, true, true));
        return map;
    }

    @Override
    public void setProperty(String string, Object obj) {
        propertiesMap.put(string, obj);
    }

    @Override
    public Object getProperty(String key) {
        return propertiesMap.get(key);
    }

    @Override
    public String getDescription() {
        return "Instantiate new scenario";
    }

    @Override
    public ActuatorResult action(SessionContext testSessionContext) {
        log.info("execute "+ getName() + ", action type:" +this.getClass().getName());

        boolean testSuccess = true;

        //if you add HTTP authentication on the BN server you need to pass these credentials
//        String user = getProperty(USER_NAME) == null ? "user" : (String) getProperty(USER_NAME);
//        String password = getProperty(USER_PASSWORD) == null ? "password" : (String) getProperty(USER_PASSWORD);
        String server = (String) getProperty(SERVER_ADDRESS);
        if(server.endsWith("/")){
            server = server.substring(0, server.lastIndexOf("/"));
        }

        if(server == null) {
            String errorMessage = "error in the configuration of the sensor " + getDescription();
            log.error(errorMessage);
            throw new RuntimeException(errorMessage);
        }

        try {
            url = new URL(server);
        } catch (MalformedURLException e) {
            log.error(e.getLocalizedMessage());
            throw new RuntimeException(e);
        }

        //"name=test1&target=CONNECTION&resource=FRODO&network=internet.bif&start=false&condition=0.99,0,OK&frequency=10&type=diagnosis"
        URLConnection connection = null;
        String charset = "UTF-8";
        String name = getProperty(SCENARIO_NAME) == null? "scenario started by the action": (String) getProperty(SCENARIO_NAME);
        String type = getProperty(TYPE) == null? "diagnosis": (String) getProperty(TYPE);
        String resource = getProperty(RESOURCE) == null? "resource": (String) getProperty(RESOURCE);
        String start = getProperty(START) == null? "false": getProperty(START).toString();
        int frequency = getProperty(FREQUENCY) == null? 15: Utils.getDouble(getProperty(FREQUENCY)).intValue();
        int operator = getProperty(OPERATOR) == null? 0: Utils.getDouble(getProperty(OPERATOR)).intValue();
        double threshold = getProperty(THRESHOLD) == null? 0.99: Utils.getDouble(getProperty(FREQUENCY)).doubleValue();

        JSONObject jsonObject = new JSONObject() ;
        JSONObject conditionObj = new JSONObject() ;
        conditionObj.put("threshold", threshold);
        conditionObj.put("operator", operator);
        conditionObj.put("stopState", getProperty(STOP_STATE));

        jsonObject.put("network", getProperty(NETWORK).toString());
        jsonObject.put("condition", conditionObj);
        jsonObject.put("start", Boolean.valueOf(start));
        jsonObject.put("resource", resource);
        jsonObject.put("type", type);
        jsonObject.put("start", Boolean.valueOf(start));
        jsonObject.put("name", name);
        jsonObject.put("target", getProperty(TARGET));
        jsonObject.put("frequency", frequency);
        String query = null;
        try {
            query = String.format("request=%s", URLEncoder.encode(jsonObject.toJSONString(), charset));
        } catch (UnsupportedEncodingException e) {
            log.error(e.getLocalizedMessage());
            testSuccess = false;
        }

        try {
            connection = url.openConnection();
        } catch (IOException e) {
            log.error(e.getLocalizedMessage());
            testSuccess = false;
        }
        connection.setDoOutput(true); // Triggers POST.
        connection.setRequestProperty("Accept-Charset", charset);
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        OutputStream output = null;
        try {
            output = connection.getOutputStream();
            output.write(query.getBytes(charset));
        } catch (IOException e) {
            log.error(e.getLocalizedMessage());
            testSuccess = false;
        } finally {
            if (output != null)
                try {
                    output.flush();
                    output.close();
                } catch (IOException e) {
                    log.error(e.getLocalizedMessage());
                }
        }

        //Get Response
        InputStream is = null;
        if(connection != null){
            try {
                is = connection.getInputStream();
            } catch (IOException e) {
                log.error(e.getLocalizedMessage());
                e.printStackTrace();
            }
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuffer response = new StringBuffer();
            try {
                while((line = rd.readLine()) != null) {
                    response.append(line);
                    response.append('\r');
                }
            } catch (IOException e) {
                log.error(e.getLocalizedMessage());
            }
            try {
                rd.close();
            } catch (IOException e) {
                log.error(e.getLocalizedMessage());
            }
            log.debug("response from the server: " + response.toString());
        }

        final boolean finalTestSuccess = testSuccess;
        return new ActuatorResult() {
            @Override
            public boolean isSuccess() {
                return finalTestSuccess;
            }

            @Override
            public String getObserverState() {
                return null;
            }
        };
    }

    @Override
    public String getName() {
        return NAME;
    }

    public static void main(String[] args) {
        ScenarioFactoryAction scenarioFactoryAction = new ScenarioFactoryAction();
        scenarioFactoryAction.setProperty(NETWORK, "RecipeRandomTest.json");
        scenarioFactoryAction.setProperty(TARGET, "Target");
        scenarioFactoryAction.setProperty(STOP_STATE, "trigger");
        scenarioFactoryAction.setProperty(OPERATOR, 0);
        scenarioFactoryAction.setProperty(THRESHOLD, 0.99);
        scenarioFactoryAction.setProperty(RESOURCE, "werewrwer");
        scenarioFactoryAction.setProperty(FREQUENCY, 60);
        scenarioFactoryAction.setProperty(TYPE, "diagnosis");
        scenarioFactoryAction.setProperty(SCENARIO_NAME, "hello world");
        scenarioFactoryAction.setProperty(SERVER_ADDRESS, "http://54.235.253.99/api");

        scenarioFactoryAction.action(null);

    }
}
