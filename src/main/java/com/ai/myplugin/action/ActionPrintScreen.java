/**
 * User: Veselin Pizurica
 * Date 08/03/2012
 */

package com.ai.myplugin.action;

import com.ai.api.*;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * FIXME what does this do?
 */
@PluginImplementation
public class ActionPrintScreen implements ActuatorPlugin{
    private static final Logger log = LoggerFactory.getLogger(ActionPrintScreen.class);

    private static final String DUMMY_PROPERTY = "dummy property that will be print out";
    private static final String NAME = "PrintOnStdoutXX";
    Map<String, Object> propertiesMap = new HashMap<>();

    @Override
    public Map<String,PropertyType> getRequiredProperties() {
        Map<String,PropertyType> map = new HashMap<>();
        map.put(DUMMY_PROPERTY, new PropertyType(DataType.STRING, true, false));
        return map;
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
        return ActuatorSuccessResult.INSTANCE;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
