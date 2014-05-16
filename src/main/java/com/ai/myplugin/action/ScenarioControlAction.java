/**
 * User: pizuricv
 * Date: 11/26/12
 */

package com.ai.myplugin.action;

import com.ai.api.ActuatorPlugin;
import com.ai.api.ActuatorResult;
import com.ai.api.SessionContext;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@PluginImplementation
public class ScenarioControlAction implements ActuatorPlugin{
    private static final Log log = LogFactory.getLog(ScenarioControlAction.class);
    private static final String SERVER_ADDRESS = "server address";
    private static final String USER_NAME = "username";
    private static final String USER_PASSWORD = "password";
    private static final String SCENARIO_ID = "scenarioID";
    private static final String COMMAND = "command";
    private static final String NAME = "ScenarioControl";
    private URL url;
    Map<String, Object> propertiesMap = new ConcurrentHashMap<String, java.lang.Object>();
    
    @Override
    public String[] getRequiredProperties() {
        return new String[]{SCENARIO_ID, SERVER_ADDRESS, USER_NAME, USER_PASSWORD, COMMAND};
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

        boolean testSuccess = true;
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

        try {
            url = new URL(server+ "/scenarios/" + scenarioID);
        } catch (MalformedURLException e) {
            log.error(e.getLocalizedMessage());
            throw new RuntimeException(e);
        }

        URLConnection connection = null;
        String charset = "UTF-8";
        String query = null;
        try {
            query = String.format("action=%s", URLEncoder.encode((String) getProperty(COMMAND), charset));
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
            log.debug(response.toString());
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
}
