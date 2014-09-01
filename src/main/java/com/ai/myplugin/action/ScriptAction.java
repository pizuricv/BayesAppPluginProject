package com.ai.myplugin.action;

import com.ai.api.*;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.*;
import java.util.HashMap;
import java.util.Map;

/**
 * http://docs.oracle.com/javase/6/docs/technotes/guides/scripting/programmer_guide/
 *
 * TODO how do we list the available engine options to the user?
 */
@PluginImplementation
public class ScriptAction implements ActuatorPlugin {

    private static final Logger log = LoggerFactory.getLogger(ScriptAction.class);

    public static final String PROPERTY_SCRIPT = "script";
    public static final String PROPERTY_ENGINE = "engine";

    private static final ScriptEngineManager manager = new ScriptEngineManager();

    private String script = "";
    private String engine = "JavaScript";

    public ScriptAction(){
        log.info("Engine list:");
        for(ScriptEngineFactory factory: manager.getEngineFactories()){
            log.info("Found engine: " + factory.getEngineName() + " -> " + factory.getNames());
        }
    }

    @Override
    public Map<String,PropertyType> getRequiredProperties() {
        Map<String,PropertyType> map = new HashMap<>();
        map.put(PROPERTY_SCRIPT, new PropertyType(DataType.STRING, true, true));
        map.put(PROPERTY_ENGINE, new PropertyType(DataType.STRING, false, false));
        return map;
    }

    @Override
    public ActuatorResult action(SessionContext sessionContext) {
        ScriptEngine scriptEngine = manager.getEngineByName(engine);
        if(scriptEngine == null){
            throw new RuntimeException("No engine available for name: " + engine);
        }
        try {
            Object result = scriptEngine.eval(script);
            log.info("Script result = " + result);
        } catch (ScriptException e) {
            log.error(e.getMessage(), e);
            return new ActuatorFailedResult(e.getMessage());
        }
        return ActuatorSuccessResult.INSTANCE;
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public void setProperty(String s, Object o) {
        switch(s){
            case PROPERTY_SCRIPT:
                this.script = String.valueOf(o);
                break;
            case PROPERTY_ENGINE:
                this.engine = String.valueOf(o);
                break;
            default:
                // ignore for now?
        }
    }

    @Override
    public Object getProperty(String s) {
        switch(s){
            case PROPERTY_SCRIPT:
                return script;
            case PROPERTY_ENGINE:
                return engine;
            default:
                return null;
        }
    }

    @Override
    public String getDescription() {
        return "The script action invokes the jvm scripting api. For more info see http://docs.oracle.com/javase/6/docs/technotes/guides/scripting/programmer_guide/";
    }
}
