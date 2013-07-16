/**
 * User: pizuricv
 * Date: 11/26/12
 */

package com.ai.myplugin.action;

import com.ai.bayes.plugins.BNActionPlugin;
import com.ai.bayes.scenario.ActionResult;
import com.ai.util.resource.TestSessionContext;
import net.xeoh.plugins.base.annotations.PluginImplementation;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@PluginImplementation
public class ScenarioControlAction implements BNActionPlugin{
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
            System.out.println("property " + string + " not known by the action");
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
    public ActionResult action(TestSessionContext testSessionContext) {
        System.out.println("####### action triggered " + getDescription());

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
            System.err.println(e.getLocalizedMessage());
            throw new RuntimeException(e);
        }
        if(server == null || scenarioID.equals(-1)) {
            String errorMessage = "error in the configuration of the sensor " + getDescription();
            System.err.println(errorMessage);
            throw new RuntimeException(errorMessage);
        }

        try {
            url = new URL(server+ "/scenarios/" + scenarioID);
        } catch (MalformedURLException e) {
            System.err.println(e.getLocalizedMessage());
            throw new RuntimeException(e);
        }

        URLConnection connection = null;
        String charset = "UTF-8";
        String query = null;
        try {
            query = String.format("action=%s", URLEncoder.encode((String) getProperty(COMMAND), charset));
        } catch (UnsupportedEncodingException e) {
            System.err.println(e.getLocalizedMessage());
            testSuccess = false;
        }
        try {
            connection = url.openConnection();
        } catch (IOException e) {
            System.err.println(e.getLocalizedMessage());
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
            System.err.println(e.getLocalizedMessage());
            testSuccess = false;
        } finally {
            if (output != null)
                try {
                    output.flush();
                    output.close();
                } catch (IOException e) {
                    System.err.println(e.getLocalizedMessage());
                }
        }

        //Get Response
        InputStream is = null;
        if(connection != null){
            try {
                is = connection.getInputStream();
            } catch (IOException e) {
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
                System.err.println(e.getLocalizedMessage());
            }
            try {
                rd.close();
            } catch (IOException e) {
                System.err.println(e.getLocalizedMessage());
            }
            System.out.println(response.toString());
        }

        final boolean finalTestSuccess = testSuccess;
        return new ActionResult() {
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
