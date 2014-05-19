/**
 * Created with IntelliJ IDEA.
 * User: veselin
 * Date: 07/09/13
 */
package com.ai.myplugin.sensor;

import com.ai.api.SensorPlugin;
import com.ai.api.SensorResult;
import com.ai.api.SessionContext;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


@PluginImplementation

public class ScriptSensor implements SensorPlugin {
    private static final Log log = LogFactory.getLog(ScriptSensor.class);
    private String scriptBody;

    @Override
    public String[] getRequiredProperties() {
        return new String []{"script"};
    }

    @Override
    public String[] getRuntimeProperties() {
        return new String[]{};
    }

    @Override
    public void setProperty(String s, Object o) {
        if("script".equals(s))
            scriptBody = (String) o;
        else throw new RuntimeException("Property " + s + " not in the required settings");
    }

    @Override
    public Object getProperty(String s) {
        return scriptBody;
    }

    @Override
    public String getDescription() {
        return "Script >> to be implemented";
    }

    @Override
    public SensorResult execute(SessionContext testSessionContext) {
        log.debug("execute " + getName() + ", sensor type:" + this.getClass().getName());
        return null;
    }

    @Override
    public String getName() {
        return "Script";
    }

    @Override
    public String[] getSupportedStates() {
        return new String[0];
    }

    @Override
    public void setup(SessionContext testSessionContext) {
        log.debug("Setup : " + getName() + ", sensor : "+this.getClass().getName());
    }

    @Override
    public void shutdown(SessionContext testSessionContext) {
        log.debug("Shutdown : " + getName() + ", sensor : " + this.getClass().getName());
    }
}
