/**
 * User: Veselin Pizurica
 * Date 08/03/2012
 */

package com.ai.myplugin.action;

import com.ai.api.ActuatorPlugin;
import com.ai.api.ActuatorResult;
import com.ai.api.SessionContext;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.util.HashMap;
import java.util.Map;


@PluginImplementation
public class ActionPrintScreen implements ActuatorPlugin{
    private static final Log log = LogFactory.getLog(ActionPrintScreen.class);

    private static final String DUMMY_PROPERTY = "dummy property that will be print out";
    private static final String NAME = "PrintOnStdoutXX";
    Map<String, Object> propertiesMap = new HashMap<String, Object>();

    @Override
    public String[] getRequiredProperties() {
        return new String[]{DUMMY_PROPERTY};
    }

    @Override
    public void setProperty(String string, Object obj) {
        if(string.equalsIgnoreCase(DUMMY_PROPERTY)) {
            propertiesMap.put(DUMMY_PROPERTY,obj);
        }
    }

    @Override
    public Object getProperty(String string) {
        return propertiesMap.get(string);
    }

    @Override
    public String getDescription() {
        return "Action that only prints the event";
    }

    @Override
    public ActuatorResult action(SessionContext testSessionContext) {
        if(propertiesMap.containsKey(DUMMY_PROPERTY)){
            log.debug("###########DUMMY ACTION#######" + propertiesMap.get(DUMMY_PROPERTY));
        }  else{
            log.debug("###########DUMMY ACTION#######");
        }

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
