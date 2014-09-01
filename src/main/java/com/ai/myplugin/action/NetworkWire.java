/**
 * User: Veselin Pizurica
 * Date 10/08/2012
 */

package com.ai.myplugin.action;

import com.ai.api.*;
import com.ai.myplugin.util.Rest;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@PluginImplementation
@PluginHeader(version = "1.0.1", author = "Veselin", category = "Java Script", iconURL = "http://app.waylay.io/icons/graph.png")
public class NetworkWire implements ActuatorPlugin{
    private static final Logger log = LoggerFactory.getLogger(NetworkWire.class);

    public static final String SERVER_ADDRESS = "server address";
    private static final String USER_NAME = "username";
    private static final String USER_PASSWORD = "password";
    public static final String SCENARIO_ID = "scenarioID";
    private static final String NAME = "NetworkStateFlooding";

    private final Map<String, Object> propertiesMap = new ConcurrentHashMap<String, Object>();


    @Override
    public Map<String,PropertyType> getRequiredProperties() {
        Map<String,PropertyType> map = new HashMap<>();
        map.put(SCENARIO_ID, new PropertyType(DataType.LONG, true, true));
        map.put(SERVER_ADDRESS, new PropertyType(DataType.STRING, true, true));
        map.put(USER_NAME, new PropertyType(DataType.STRING, true, true));
        map.put(USER_PASSWORD, new PropertyType(DataType.STRING, true, true));
        return map;
    }

    @Override
    public void setProperty(String string, Object obj) {
        if(string.equalsIgnoreCase(SCENARIO_ID) || string.equalsIgnoreCase(SERVER_ADDRESS)
                || string.equalsIgnoreCase(USER_NAME) || string.equalsIgnoreCase(USER_PASSWORD)) {
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
        return "Action that fires the triggered state towards another network";
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
            return new ActuatorFailedResult("Could not parse the " + SCENARIO_ID + " to int: " + e.getMessage());
        }
        if(server == null || scenarioID == null) {
            String errorMessage = "error in the configuration of the sensor " + getDescription();
            log.error(errorMessage);
            return new ActuatorFailedResult(errorMessage);
        }

        ///scenarios/{scenario}/{node}
        String node = (String) testSessionContext.getAttribute(SessionParams.NODE_NAME);
        String state = (String) testSessionContext.getAttribute(SessionParams.NODE_TRIGGERED_STATE);

        String charset = "UTF-8";
        String query;
        try {
            query = String.format("state=%s", URLEncoder.encode(state, charset));
        } catch (UnsupportedEncodingException e) {
            log.error(e.getLocalizedMessage());
            return new ActuatorFailedResult(e.getMessage());
        }
        if(server.endsWith("/")){
            server = server.substring(0, server.lastIndexOf("/"));
        }
        String url = server+ "/scenarios/" + scenarioID + "/"+ node;
        try {
            Rest.RestReponse response = Rest.httpPost(url, query, charset);
            return ActuatorSuccessResult.INSTANCE;
        } catch (IOException e) {
            return new ActuatorFailedResult(e.getMessage());
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

}
