/**
 * User: pizuricv
 * Date: 10/29/12
 */
package com.ai.myplugin.action;

import com.ai.api.ActuatorPlugin;
import com.ai.api.ActuatorResult;
import com.ai.api.PropertyType;
import com.ai.api.SessionContext;
import net.xeoh.plugins.base.annotations.PluginImplementation;

import java.util.HashMap;
import java.util.Map;

@PluginImplementation
public class BeepAction implements ActuatorPlugin{
    private static final String NAME = "Beep";
    @Override
    public Map<String, PropertyType> getRequiredProperties() {
        return new HashMap<>();
    }

    @Override
    public void setProperty(String s, Object o) {
    }

    @Override
    public Object getProperty(String s) {
        return null;
    }

    @Override
    public String getDescription() {
        return "Simple beep signal";
    }

    @Override
    public ActuatorResult action(SessionContext testSessionContext) {
        java.awt.Toolkit.getDefaultToolkit().beep();
        return new ActuatorResult() {
            @Override
            public boolean isSuccess() {
                return true;
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
