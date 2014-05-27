/**
 * User: Veselin Pizurica
 * Date 10/08/2012
 */

package com.ai.myplugin.action;

import com.ai.api.*;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@PluginImplementation
public class NetworkWire implements ActuatorPlugin{
    private static final Log log = LogFactory.getLog(NetworkWire.class);

    private static final String SERVER_ADDRESS = "server address";
    private static final String USER_NAME = "username";
    private static final String USER_PASSWORD = "password";
    private static final String SCENARIO_ID = "scenarioID";
    private static final String NAME = "NetworkStateFlooding";

    private URL url;
    Map<String, Object> propertiesMap = new ConcurrentHashMap<String, Object>();


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
    public void action(SessionContext testSessionContext) {
        log.info("execute "+ getName() + ", action type:" +this.getClass().getName());

        Integer scenarioID = -1;

        //if you add HTTP authentication on the BN server you need to pass these credentials
//        String user = getProperty(USER_NAME) == null ? "user" : (String) getProperty(USER_NAME);
//        String password = getProperty(USER_PASSWORD) == null ? "password" : (String) getProperty(USER_PASSWORD);
        String server = (String) getProperty(SERVER_ADDRESS);
        if(server.endsWith("/")){
            server = server.substring(0, server.lastIndexOf("/"));
        }

        try {
            scenarioID = Integer.parseInt((String) getProperty(SCENARIO_ID));
        } catch (Exception e){
            log.error(e.getLocalizedMessage());
            throw new RuntimeException(e);
        }
        if(server == null || scenarioID.equals(-1)) {
            String errorMessage = "error in the configuration of the sensor " + getDescription();
            log.error(errorMessage);
            throw new RuntimeException(errorMessage);
        }

        ///scenarios/{scenario}/{node}
        String node = (String) testSessionContext.getAttribute(SessionParams.NODE_NAME);
        String state = (String) testSessionContext.getAttribute(SessionParams.NODE_TRIGGERED_STATE);

        try {
            url = new URL(server+ "/scenarios/" + scenarioID + "/"+ node);
        } catch (MalformedURLException e) {
            log.error(e.getLocalizedMessage());
            throw new RuntimeException(e);
        }

        URLConnection connection = null;
        String charset = "UTF-8";
        String query = null;
        try {
            query = String.format("state=%s", URLEncoder.encode(state, charset));
        } catch (UnsupportedEncodingException e) {
            log.error(e.getLocalizedMessage());
            throw new RuntimeException(e);
        }
        try {
            connection = url.openConnection();
        } catch (IOException e) {
            log.error(e.getLocalizedMessage());
            throw new RuntimeException(e);
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
            throw new RuntimeException(e);
        } finally {
            if (output != null)
                try {
                    output.flush();
                    output.close();
                } catch (IOException e) {
                    log.error(e.getLocalizedMessage());
                    throw new RuntimeException(e);
                }
        }

        //Get Response
        InputStream is = null;
        if(connection != null){
            try {
                is = connection.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
                log.error(e.getLocalizedMessage());
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
            log.debug(response.toString());
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    public static void main(String [] args ){
        NetworkWire networkWire = new NetworkWire();
        networkWire.setProperty(SERVER_ADDRESS, "http://85.255.197.153/api");
        networkWire.setProperty(SCENARIO_ID, "1");
        SessionContext testSessionContext =  new SessionContext(1);
        testSessionContext.setAttribute(SessionParams.NODE_NAME, "CONNECTION");
        testSessionContext.setAttribute(SessionParams.NODE_TRIGGERED_STATE, "NOK");
        networkWire.action(testSessionContext);
    }


}
