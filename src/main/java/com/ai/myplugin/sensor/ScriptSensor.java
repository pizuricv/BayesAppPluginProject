package com.ai.myplugin.sensor; /**
 * Created with IntelliJ IDEA.
 * User: veselin
 * Date: 07/09/13
 */

import com.ai.bayes.plugins.BNSensorPlugin;
import com.ai.bayes.scenario.TestResult;
import com.ai.util.resource.TestSessionContext;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


@PluginImplementation

public class ScriptSensor implements BNSensorPlugin {
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
    public TestResult execute(TestSessionContext testSessionContext) {
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
    public void shutdown(TestSessionContext testSessionContext) {
        log.debug("Shutdown : " + getName() + ", sensor : " + this.getClass().getName());
    }
}
