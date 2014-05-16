/**
 * User: pizuricv
 */

package com.ai.myplugin.action;

import com.ai.api.ActuatorPlugin;
import com.ai.api.ActuatorResult;
import com.ai.api.SessionContext;
import net.xeoh.plugins.base.annotations.PluginImplementation;

import javax.swing.*;

@PluginImplementation
public class AlarmDialog implements ActuatorPlugin {

    private static final String NAME = "AlarmDialog";

    private String alarmMessage = null;

    public String[] getRequiredProperties() {
        return new String[] {"Message to show"};
    }

    public void setProperty(String string, Object obj) {
        alarmMessage = obj.toString();

    }

    public Object getProperty(String string) {
        return alarmMessage;
    }

    public String getDescription() {
        return "Swing Action that let you show the message on the screen";
    }

    private String getAlarmMessage(){
        return alarmMessage == null || alarmMessage.startsWith("No Value")? "Alarm message": alarmMessage;
    }

    @Override
    public ActuatorResult action(SessionContext testSessionContext) {
        JOptionPane.showMessageDialog(null, getAlarmMessage(), "Action dialog", JOptionPane.ERROR_MESSAGE);
        return new ActuatorResult() {
            @Override
            public boolean isSuccess() {
                return true;
            }

            @Override
            public String getObserverState() {
                return null;
            }
        } ;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
