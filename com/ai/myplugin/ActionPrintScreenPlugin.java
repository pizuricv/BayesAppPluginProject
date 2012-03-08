/**
 * User: Veselin Pizurica
 * Date 08/03/2012
 */

package com.ai.myplugin;

import com.ai.bayes.scenario.Receiver;
import com.ai.bayes.scenario.ActionResult;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import com.ai.bayes.plugins.BNActionPlugin;
import java.util.HashMap;
import java.util.Map;
import java.lang.System;

@PluginImplementation
public class ActionPrintScreenPlugin implements BNActionPlugin{

    private static final String DUMMY_PROPERTY = "dummy property that will be print out";
    Map<String, Object> propertiesMap = new HashMap<String, Object>();

    @Override
    public String[] getRequiredProperties() {
        return new String[]{DUMMY_PROPERTY};
    }

    @Override
    public void setProperty(String string, Object obj) {
        if(string.equals(DUMMY_PROPERTY)) {
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
    public BNActionPlugin getNewInstance() {
        return new ActionPrintScreenPlugin();
    }

    @Override
    public ActionResult action(Receiver receiver) {
        if(propertiesMap.containsKey(DUMMY_PROPERTY)){
            System.out.println("###########DUMMY ACTION#######"+ propertiesMap.get(DUMMY_PROPERTY));
        }  else{
            System.out.println("###########DUMMY ACTION#######");
        }

        if(receiver != null){
            return receiver.action();
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
        return "Print Screen Action V1";
    }
}
