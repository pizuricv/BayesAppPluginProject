/**
 * Created by User: veselin
 * On Date: 12/01/14
 */

package com.ai.myplugin.sensor;

import com.ai.api.*;
import com.ai.myplugin.util.*;
import net.xeoh.plugins.base.annotations.PluginImplementation;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Created by User: veselin
 * On Date: 26/12/13
 */
@PluginImplementation
@PluginHeader(version = "1.0.1", author = "Veselin", category = "IOT", iconURL = "http://app.waylay.io/icons/force.png")
public class ForceSensor implements SensorPlugin {

    private static final Logger log = LoggerFactory.getLogger(ForceSensor.class);

    static final String FORCE_THRESHOLD = "force_threshold";
    static final String RUNTIME_FORCE = "runtime_force";

    Map<String, Object> propertiesMap = new ConcurrentHashMap<String, Object>();

    String [] states = {"Below", "Above"};
    private static final String NAME = "Force";

    @Override
    public Map<String, PropertyType> getRequiredProperties() {
        Map<String, PropertyType> map = new HashMap<>();
        map.put(FORCE_THRESHOLD, new PropertyType(DataType.DOUBLE, true, false));
        return map;
    }

    @Override
    public Map<String, PropertyType> getRuntimeProperties() {
        Map<String, PropertyType> map = new HashMap<>();
        map.put(RUNTIME_FORCE, new PropertyType(DataType.DOUBLE, true, false));
        return map;
    }

    @Override
    public void setProperty(String string, Object obj) {
        if(getRequiredProperties().keySet().contains(string)) {
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
        return "Force sensor, check if the measurement is above the threshold";
    }

    @Override
    public SensorResult execute(SessionContext testSessionContext) {
        log.info("execute "+ getName() + ", sensor type:" +this.getClass().getName());
        if(getProperty(FORCE_THRESHOLD) == null)
            throw new RuntimeException("distance not set");

        Object rt1 = testSessionContext.getAttribute(RUNTIME_FORCE);
        if(rt1 == null){
            log.warn("no runtime force given");
            return new EmptySensorResult();
        }
        Double runtime_force = Utils.getDouble(rt1);
        log.info("Current force: " + runtime_force);

        JSONObject jsonObject = new JSONObject();

        Double configuredThreshold = Utils.getDouble(getProperty(FORCE_THRESHOLD));

        jsonObject.put("runtime_force", runtime_force);
        jsonObject.put("configured_threshold", configuredThreshold);

        final String state;
        if(configuredThreshold > runtime_force)
            state = states[0];
        else
            state = states[1];

        final JSONObject finalJsonObject = jsonObject;
        return new SensorResult() {
            @Override
            public boolean isSuccess() {
                return true;
            }

            @Override
            public String getName() {
                return "Force result";
            }

            @Override
            public String getObserverState() {
                return state;
            }

            @Override
            public List<Map<String, Number>> getObserverStates() {
                return null;
            }

            @Override
            public String getRawData() {
                return finalJsonObject.toJSONString();
            }
        };

    }

    @Override
    public void setup(SessionContext testSessionContext) {
        log.debug("Setup : " + getName() + ", sensor : "+this.getClass().getName());
    }

    @Override
    public void shutdown(SessionContext testSessionContext) {

    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Set<String> getSupportedStates() {
        return new HashSet(Arrays.asList(states));
    }

    public static void main(String []args) throws ParseException {
        ForceSensor forceSensor = new ForceSensor();
        forceSensor.setProperty(FORCE_THRESHOLD, 5);
        SessionContext testSessionContext = new SessionContext(1);
        testSessionContext.setAttribute(RUNTIME_FORCE, 19.851858);
        SensorResult testResult = forceSensor.execute(testSessionContext);
        System.out.println(testResult.getObserverState());
        System.out.println(testResult.getRawData());
        testSessionContext.setAttribute(RUNTIME_FORCE, 1);
        testResult = forceSensor.execute(testSessionContext);
        System.out.println(testResult.getObserverState());
        System.out.println(testResult.getRawData());
    }
}
