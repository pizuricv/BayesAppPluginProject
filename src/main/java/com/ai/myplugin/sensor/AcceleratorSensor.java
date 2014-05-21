/**
 * Created by User: veselin
 * On Date: 12/01/14
 */

package com.ai.myplugin.sensor;

import com.ai.api.*;
import com.ai.myplugin.util.*;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Created by User: veselin
 * On Date: 26/12/13
 */
@PluginImplementation
public class AcceleratorSensor implements SensorPlugin {
    protected static final Log log = LogFactory.getLog(AcceleratorSensor.class);
    static final String ACCELERATOR_THRESHOLD = "accelerator_threshold";
    static final String RUNTIME_ACCELERATOR = "runtime_accelerator";

    Map<String, Object> propertiesMap = new ConcurrentHashMap<String, Object>();

    String [] states = {"Below", "Above"};
    private static final String NAME = "AccelerationSensor";

    @Override
    public Map<String, PropertyType> getRequiredProperties() {
        Map<String, PropertyType> map = new HashMap<>();
        map.put(ACCELERATOR_THRESHOLD, new PropertyType(DataType.DOUBLE, true, false));
        return map;
    }

    @Override
    public Map<String, PropertyType> getRuntimeProperties() {
        Map<String, PropertyType> map = new HashMap<>();
        map.put(RUNTIME_ACCELERATOR, new PropertyType(DataType.DOUBLE, true, false));
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
        return "Accelerator sensor, check if the measurement is above the threshold";
    }

    @Override
    public SensorResult execute(SessionContext testSessionContext) {
        log.info("execute "+ getName() + ", sensor type:" +this.getClass().getName());
        if(getProperty(ACCELERATOR_THRESHOLD) == null)
            throw new RuntimeException("acceleration threshold not set");

        Object rt1 = testSessionContext.getAttribute(RUNTIME_ACCELERATOR);
        if(rt1 == null){
            log.warn("no runtime acceleration given");
            return new EmptySensorResult();
        }
        Double runtime_force = Utils.getDouble(rt1);
        log.info("Current force: " + runtime_force);

        JSONObject jsonObject = new JSONObject();

        Double configuredThreshold = Utils.getDouble(getProperty(ACCELERATOR_THRESHOLD));


        jsonObject.put(RUNTIME_ACCELERATOR, runtime_force);
        jsonObject.put(ACCELERATOR_THRESHOLD, configuredThreshold);

        final String state;
        if(configuredThreshold  > runtime_force)
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
                return "Accelerator result";
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
        log.debug("Shutdown : " + getName() + ", sensor : "+this.getClass().getName());
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String[] getSupportedStates() {
        return states;
    }

    public static void main(String []args) throws ParseException {
        AcceleratorSensor acceleratorSensor = new AcceleratorSensor();
        acceleratorSensor.setProperty(ACCELERATOR_THRESHOLD, 5);
        SessionContext testSessionContext = new SessionContext(1);
        testSessionContext.setAttribute(RUNTIME_ACCELERATOR, 19.851858);
        SensorResult testResult = acceleratorSensor.execute(testSessionContext);
        System.out.println(testResult.getObserverState());
        System.out.println(testResult.getRawData());
        testSessionContext.setAttribute(RUNTIME_ACCELERATOR, 1);
        testResult = acceleratorSensor.execute(testSessionContext);
        System.out.println(testResult.getObserverState());
        System.out.println(testResult.getRawData());
    }
}
