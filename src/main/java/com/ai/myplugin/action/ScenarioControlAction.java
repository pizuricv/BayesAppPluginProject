/**
 * User: pizuricv
 * Date: 11/26/12
 */

package com.ai.myplugin.action;

import com.ai.api.*;
import com.ai.myplugin.util.Rest;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@PluginImplementation
@PluginHeader(version = "1.0.1", author = "Veselin", category = "Java Script", iconURL = "http://app.waylay.io/icons/control.png")
public class ScenarioControlAction implements ActuatorPlugin{

    private static final Logger log = LoggerFactory.getLogger(ScenarioControlAction.class);

    private static final String SERVER_ADDRESS = "server address";
    private static final String USER_NAME = "username";
    private static final String USER_PASSWORD = "password";
    private static final String SCENARIO_ID = "scenarioID";
    private static final String COMMAND = "command";
    private static final String NAME = "ScenarioControl";
    private URL url;
    Map<String, Object> propertiesMap = new ConcurrentHashMap<String, java.lang.Object>();

    @Override
    public Map<String,PropertyType> getRequiredProperties() {
        Map<String,PropertyType> map = new HashMap<>();
        map.put(SCENARIO_ID, new PropertyType(DataType.LONG, true, true));
        map.put(COMMAND, new PropertyType(DataType.STRING, true, true));
        map.put(SERVER_ADDRESS, new PropertyType(DataType.STRING, true, true));
        map.put(USER_NAME, new PropertyType(DataType.STRING, true, true));
        map.put(USER_PASSWORD, new PropertyType(DataType.STRING, true, true));
        return map;
    }

    @Override
    public void setProperty(String string, Object obj) {
        if(string.equals(SCENARIO_ID) || string.equals(SERVER_ADDRESS)
                || string.equals(USER_NAME) || string.equals(USER_PASSWORD) || string.equals(COMMAND)) {
            propertiesMap.put(string, obj);
        } else {
            throw new RuntimeException("Property "+ string + " not in the required settings");
        }
    }

    @Override
    public Object getProperty(String key) {
        return propertiesMap.get(key);
    }

    @Override
    public String getDescription() {
        return "Allows start/stop/pause/delete of existing scenarios";
    }

    @Override
    public ActuatorResult action(SessionContext testSessionContext) {
        log.info("execute "+ getName() + ", action type:" +this.getClass().getName());

        //if you add HTTP authentication on the BN server you need to pass these credentials
//        String user = getProperty(USER_NAME) == null ? "user" : (String) getProperty(USER_NAME);
//        String password = getProperty(USER_PASSWORD) == null ? "password" : (String) getProperty(USER_PASSWORD);
        String server = (String) getProperty(SERVER_ADDRESS);

        Integer scenarioID = null;
        try {
            scenarioID = Integer.parseInt((String) getProperty(SCENARIO_ID));
        } catch (NumberFormatException e){
            log.error(e.getLocalizedMessage(), e);
            return new ActuatorFailedResult(e.getMessage());
        }
        if(server == null || scenarioID == null) {
            return new ActuatorFailedResult("error in the configuration of the sensor " + getDescription());
        }

        String charset = "UTF-8";
        String query;
        try {
            query = String.format("action=%s", URLEncoder.encode((String) getProperty(COMMAND), charset));
        } catch (UnsupportedEncodingException e) {
            log.error(e.getLocalizedMessage(), e);
            return new ActuatorFailedResult(e.getMessage());
        }

        if(server.endsWith("/")){
            server = server.substring(0, server.lastIndexOf("/"));
        }
        String url = server + "/scenarios/" + scenarioID;
        try {
            Rest.RestReponse response = Rest.httpPost(url, query, charset);
            return ActuatorSuccessResult.INSTANCE;
        } catch (IOException e) {
            log.error(e.getLocalizedMessage(), e);
            return new ActuatorFailedResult(e.getMessage());
        }
    }

    @Override
    public String getName() {
        return NAME;
    }
}
