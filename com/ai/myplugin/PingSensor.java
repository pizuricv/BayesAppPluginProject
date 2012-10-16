/**
 * created by: Veselin Pizurica
 * Date: 06/03/12
 */

package com.ai.myplugin;

import com.ai.bayes.scenario.TestResult;
import com.ai.bayes.model.BayesianNetwork;
import com.ai.util.resource.TestSessionContext;
import com.ai.bayes.plugins.BNSensorPlugin;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.lang.System;


@PluginImplementation
public class PingSensor implements BNSensorPlugin{
    private static final String IP_ADDRESS = "IP address";
    private static final String TIMEOUT = "timeout";
    private static final String ALIVE = "Alive";
    private static final String NOT_ALIVE = "Not Alive";
    private String nodeName;

    Map<String, Object> propertiesMap = new HashMap<String, Object>();


    public String[] getRequiredProperties() {
        return new String[] {IP_ADDRESS, TIMEOUT};
    }

    public void setProperty(String string, Object obj) {
        if(string.equals(IP_ADDRESS)) {
            try {
                propertiesMap.put(string,InetAddress.getByName(obj.toString()));
            } catch (UnknownHostException e) {
                System.err.println(e.getLocalizedMessage());
            }
        } else if(string.equals(TIMEOUT)){
            propertiesMap.put(string, Integer.parseInt(obj.toString()));
        }
    }

    public Object getProperty(String string) {
        return propertiesMap.get(string);
    }

    public String getDescription() {
        return "Ping test to check IP connectivity";
    }

    public TestResult execute(TestSessionContext testSessionContext) {
        boolean reachable = false;
        boolean testSuccess = true;

        try {
            reachable = getAddress().isReachable(getTimeOut());
        } catch (IOException e) {
            testSuccess = false;
            System.err.println(e.getLocalizedMessage());
        }
        final boolean finalTestFailed = testSuccess;
        final boolean finalReachable = reachable;
        TestResult result = new TestResult() {
            public boolean isSuccess() {
                return finalTestFailed;
            }
            /*
            you need to return the node name, since the diagnosis result for the node is linked to the node name of the test result 
            */
            public String getName() {
                return getNodeName();
            }

            public String getObserverState() {
                if(finalReachable){
                    return ALIVE;
                } else {
                    return NOT_ALIVE;
                }
            }
        };
        return result;
    }

    /*
    Name needs to be unique across different sensors
    */
    public String getName() {
        return "Ping Test";
    }

    public String[] getSupportedStates() {
        return new String[] {ALIVE, NOT_ALIVE} ;
    }

    private InetAddress getAddress(){
        return (InetAddress) getProperty(IP_ADDRESS);
    }

    private int getTimeOut(){
        return (Integer) getProperty(TIMEOUT);
    }

    public BNSensorPlugin getNewInstance() {
        return new PingSensor();
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getNodeName() {
        return nodeName;
    }

    /*
    Only if you need the access to the belief network, otherwise you can don't need this call
    */
    public void setBayesianNetwork(BayesianNetwork bayesianNetwork) {
    }


}
