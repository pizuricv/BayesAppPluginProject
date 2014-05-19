/**
 * created by: Veselin Pizurica
 * Date: 06/03/12
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
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@PluginImplementation
public class PingSensor implements SensorPlugin {
    private static final Log log = LogFactory.getLog(PingSensor.class);

    private static final String ADDRESS = "address";
    private static final String ALIVE = "Alive";
    private static final String NOT_ALIVE = "Not Alive";
    private static final String NAME = "Ping";

    Map<String, Object> propertiesMap = new ConcurrentHashMap<String, Object>();


    public String[] getRequiredProperties() {
        return new String[] {ADDRESS};
    }

    @Override
    public String[] getRuntimeProperties() {
        return new String[]{};
    }

    public void setProperty(String string, Object obj) {
        if(string.equalsIgnoreCase(ADDRESS)) {
            propertiesMap.put(ADDRESS, obj.toString());
        }
    }

    public Object getProperty(String string) {
        return propertiesMap.get(string);
    }

    public String getDescription() {
        return "Ping test to check IP connectivity";
    }

    public SensorResult execute(SessionContext testSessionContext) {
        log.info("execute "+ getName() + ", sensor type:" +this.getClass().getName());
        /*boolean reachable = false;
        boolean testSuccess = true;

        try {
            reachable = getAddress().isReachable(getTimeOut());
        } catch (Exception e) {
            testSuccess = false;
            log.error(e.getLocalizedMessage());
        }
        final boolean finalTestFailed = testSuccess;
        final boolean finalReachable = reachable;  */
        JSONObject pingObj = null;
        try {
            pingObj = pingAddress((String) getProperty(ADDRESS));
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            return new EmptyTestResult();
        }
        final JSONObject finalPingObj = pingObj;
        final Boolean isReachable = ((String) finalPingObj.get("result")).equalsIgnoreCase("true");
        SensorResult result = new SensorResult() {
            public boolean isSuccess() {
                if(finalPingObj == null)
                    return false;
                else{
                    return isReachable;
                }
            }
            /*
            you need to return the node name, since the diagnosis result for the node is linked to the node name of the test result 
            */
            public String getName() {
                return "Ping Test Result";
            }

            public String getObserverState() {
                if(isReachable){
                    return ALIVE;
                } else {
                    return NOT_ALIVE;
                }
            }

            @Override
            public List<Map<String, Number>> getObserverStates() {
                return null;
            }

            public String getRawData(){
                return finalPingObj.toJSONString();
            }
        };
        return result;
    }

    /*
    Name needs to be unique across different sensors
    */
    public String getName() {
        return NAME;
    }

    public String[] getSupportedStates() {
        return new String[] {ALIVE, NOT_ALIVE} ;
    }

    /*
    RESPONSE
        {
          "result": "true",
          "time": "71.511"
        }
     */
    public static JSONObject pingAddress(String address) throws Exception {
        Map<String, String> map = new ConcurrentHashMap<String, String>();
        map.put("X-Mashape-Authorization", APIKeys.getMashapeKey());

        String url = "https://igor-zachetly-ping-uin.p.mashape.com/pinguin.php?address=" + URLEncoder.encode(address);
        String ret = Rest.httpGet(url, map);

        return (JSONObject) new JSONParser().parse(ret);
    }

    @Override
    public void setup(SessionContext testSessionContext) {
        log.debug("Setup : " + getName() + ", sensor : "+this.getClass().getName());
    }

    @Override
    public void shutdown(SessionContext testSessionContext) {
        log.debug("Shutdown : " + getName() + ", sensor : "+this.getClass().getName());
    }

    public static void main(String [] args){
        PingSensor pingSensor = new PingSensor();
        pingSensor.setProperty(ADDRESS, "www.waylay.io");
        SensorResult testResult = pingSensor.execute(null);
        System.out.println(testResult.getRawData());
        System.out.println(testResult.getObserverState());

        pingSensor.setProperty(ADDRESS, "www.waylaay.io");
        testResult = pingSensor.execute(null);
        System.out.println(testResult.getRawData());
        System.out.println(testResult.getObserverState());
    }
}
