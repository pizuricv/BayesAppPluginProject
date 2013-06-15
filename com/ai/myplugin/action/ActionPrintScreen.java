/**
 * User: Veselin Pizurica
 * Date 08/03/2012
 */

package com.ai.myplugin.action;

import com.ai.bayes.scenario.ActionResult;
import com.ai.util.resource.TestSessionContext;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import com.ai.bayes.plugins.BNActionPlugin;
import java.util.HashMap;
import java.util.Map;
import java.lang.System;

@PluginImplementation
public class ActionPrintScreen implements BNActionPlugin{

    private static final String DUMMY_PROPERTY = "dummy property that will be print out";
    private static final String NAME = "PrintOnStdout";
    Map<String, Object> propertiesMap = new HashMap<String, Object>();

    @Override
    public String[] getRequiredProperties() {
        return new String[]{DUMMY_PROPERTY};
    }

    @Override
    public void setProperty(String string, Object obj) {
        if(string.equalsIgnoreCase(DUMMY_PROPERTY)) {
            propertiesMap.put(string,obj);
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
    public ActionResult action(TestSessionContext testSessionContext) {
        if(propertiesMap.containsKey(DUMMY_PROPERTY)){
            System.out.println("###########DUMMY ACTION#######"+ propertiesMap.get(DUMMY_PROPERTY));
        }  else{
            System.out.println("###########DUMMY ACTION#######");
        }

        return new ActionResult() {
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
