/**
 * User: Veselin Pizurica
 * Date 10/08/2012
 */

package com.ai.myplugin;

import com.ai.bayes.scenario.ActionResult;
import com.ai.util.resource.NodeSessionParams;
import com.ai.util.resource.TestSessionContext;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import com.ai.bayes.plugins.BNActionPlugin;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.lang.System;

@PluginImplementation
public class NetworkWire implements BNActionPlugin{

    private static final String SERVER_ADDRESS = "remote server address";
    private static final String SERVER_PORT = "remote server port";
    private static final String USER_NAME = "remote server user";
    private static final String USER_PASSWORD = "remote server password";
    private static final String SCENARIO_ID = "remote scenario ID";
    private URL url;
    Map<String, Object> propertiesMap = new HashMap<String, Object>();

    @Override
    public String[] getRequiredProperties() {
        return new String[]{SCENARIO_ID, SERVER_ADDRESS, SERVER_PORT, USER_NAME, USER_PASSWORD};
    }

    @Override
    public void setProperty(String string, Object obj) {
        if(string.equals(SCENARIO_ID) || string.equals(SERVER_ADDRESS) || string.equals(SERVER_PORT) 
                || string.equals(USER_NAME) || string.equals(USER_PASSWORD)) {
            propertiesMap.put(string, obj);
        } else {
            System.out.println("property " + string + " not known by the action");
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
    public BNActionPlugin getNewInstance() {
        return new NetworkWire();
    }

    @Override
    public ActionResult action(TestSessionContext testSessionContext) {
        System.out.println("####### action triggered " + getDescription());
        
        boolean testSuccess = true;
        Integer port = -1;
        Integer scenarioID = -1;
//        String user = getProperty(USER_NAME) == null ? "user" : (String) getProperty(USER_NAME);
//        String password = getProperty(USER_PASSWORD) == null ? "password" : (String) getProperty(USER_PASSWORD);
        String server = (String) getProperty(SERVER_ADDRESS);

        try {
            port = Integer.parseInt((String) getProperty(SERVER_PORT));
            scenarioID = Integer.parseInt((String) getProperty(SCENARIO_ID));
        } catch (Exception e){
            System.err.println(e.getLocalizedMessage());
            throw new RuntimeException(e);
        }
        if(server == null || port.equals(-1) || scenarioID.equals(-1)) {
            String errorMessage = "error in the configuration of the sensor " +getDescription();
            System.err.println(errorMessage);
            throw new RuntimeException(errorMessage);
        }    

        ///scenarios/{scenario}/{node}
        String node = (String) testSessionContext.getAttribute(NodeSessionParams.NODE_NAME);
        String state = (String) testSessionContext.getAttribute(NodeSessionParams.NODE_TRIGGERED_STATE);

        try {
            url = new URL("http", server, port, "/rest/bn/scenarios/"+ scenarioID + "/"+ node);
        } catch (MalformedURLException e) {
            System.err.println(e.getLocalizedMessage());
            throw new RuntimeException(e);
        }

        URLConnection connection = null;
        String charset = "UTF-8";
        String query = null;
        try {
            query = String.format("state=%s", URLEncoder.encode(state, charset));
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
        return "Network Wire";
    }

    public static void main(String [] args ){
        NetworkWire networkWire = new NetworkWire();
        networkWire.setProperty(SERVER_PORT, "8888");
        networkWire.setProperty(SERVER_ADDRESS, "localhost");
        networkWire.setProperty(SCENARIO_ID, "1");
        TestSessionContext testSessionContext =  new TestSessionContext(1);
        testSessionContext.setAttribute(NodeSessionParams.NODE_NAME, "CONNECTION");
        testSessionContext.setAttribute(NodeSessionParams.NODE_TRIGGERED_STATE, "NOK");
        networkWire.action(testSessionContext);
    }


}
