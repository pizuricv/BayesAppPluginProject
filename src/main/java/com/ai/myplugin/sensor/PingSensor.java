/**
 * created by: Veselin Pizurica
 * Date: 06/03/12
 */

package com.ai.myplugin.sensor;

import com.ai.api.SensorPlugin;
import com.ai.api.SensorResult;
import com.ai.api.SessionContext;
import com.ai.myplugin.util.APIKeys;
import com.ai.myplugin.util.EmptySensorResult;
import com.ai.myplugin.util.Rest;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
        log.info("execute " + getName() + ", sensor type:" + this.getClass().getName());

        Optional<PingResult> resultOpt = pingWithMashape((String) getProperty(ADDRESS));
        return resultOpt.map( pingResult ->
            // TODO why do we need this cast?
                (SensorResult)new SensorResult() {
                public boolean isSuccess() {
                    return pingResult.reachable;
                }
                /*
                you need to return the node name, since the diagnosis result for the node is linked to the node name of the test result
                */
                public String getName() {
                    return "Ping Test Result";
                }

                public String getObserverState() {
                    if(pingResult.reachable){
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
                    return toJson(pingResult).toJSONString();
                }
            }
        ).orElse(new EmptySensorResult());
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


    @Override
    public void setup(SessionContext testSessionContext) {
        log.debug("Setup : " + getName() + ", sensor : " + this.getClass().getName());
    }

    @Override
    public void shutdown(SessionContext testSessionContext) {
        log.debug("Shutdown : " + getName() + ", sensor : " + this.getClass().getName());
    }

    private JSONObject toJson(PingResult result){
        // TODO we probably want a properly typed json object, not all strings
        JSONObject root = new JSONObject();
        root.put("result", String.valueOf(result.reachable));
        result.time.ifPresent(time -> root.put("time", String.valueOf(time)));
        return root;
    }

    /*
        RESPONSE
        {
          "result": "true",
          "time": "71.511"
        }
     */
    Optional<PingResult> pingWithMashape(final String address) {
        Map<String, String> map = new ConcurrentHashMap<String, String>();
        map.put("X-Mashape-Authorization", APIKeys.getMashapeKey());

        String url = "https://igor-zachetly-ping-uin.p.mashape.com/pinguin.php?address=" + URLEncoder.encode(address);

        try {
            JSONObject json = Rest.httpGet(url, map).json();
            final boolean isReachable = ((String) json.get("result")).equalsIgnoreCase("true");
            final Optional<Double> time = Optional.ofNullable(json.get("time"))
                    .map(String::valueOf)
                    .filter(t -> !t.isEmpty())
                    .map(Double::parseDouble);
            return Optional.of(new PingResult(isReachable, time));
        }catch(ParseException|IOException ex){
            log.error(ex.getMessage(), ex);
            return Optional.empty();
        }
    }

    /**
     * http://stackoverflow.com/questions/9922543/why-does-inetaddress-isreachable-return-false-when-i-can-ping-the-ip-address
     *
     * @param address
     * @return
     * @throws IOException
     */
    Optional<PingResult> pingJava(final String address){
        try {
            boolean reachable = Arrays.asList(InetAddress.getAllByName(address)).stream().anyMatch(addr -> {
                try {
                    return addr.isReachable(10000);
                } catch (IOException ex) {
                    return false;
                }
            });
            return Optional.of(new PingResult(reachable));
        } catch (IOException ex) {
            log.error(ex.getMessage(), ex);
            return Optional.empty();
        }
    }

    Optional<PingResult> pingShell(String address) {
        // TODO this is a potential security issue as a user might execute any command on the shell!!!
        Process p1 = null;
        try {
            p1 = Runtime.getRuntime().exec("ping -c 1 " + address);
            int returnVal = p1.waitFor();
            boolean reachable = (returnVal==0);
            return Optional.of(new PingResult(reachable));
        } catch (IOException|InterruptedException ex) {
            // TODO we should probably handle the InterruptedException in a cleaner way
            log.error(ex.getMessage(), ex);
            return Optional.empty();
        }
    }

    static class PingResult{
        final boolean reachable;
        final Optional<Double> time;

        PingResult(final boolean reachable, final Optional<Double> time) {
            this.reachable = reachable;
            this.time = time;
        }

        PingResult(final boolean reachable) {
            this(reachable, Optional.empty());
        }
    }
}
