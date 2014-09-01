/**
 * User: pizuricv
 */

package com.ai.myplugin.action;

import com.ai.api.*;
import net.xeoh.plugins.base.annotations.PluginImplementation;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

@PluginImplementation
public class AlarmDialog implements ActuatorPlugin {

    private static final String NAME = "AlarmDialog";

    private String alarmMessage = null;

    @Override
    public Map<String,PropertyType> getRequiredProperties() {
        Map<String,PropertyType> map = new HashMap<>();
        map.put("Message", new PropertyType(DataType.STRING, true, false));
        return map;
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

    public ActuatorResult action(SessionContext testSessionContext) {
        JOptionPane.showMessageDialog(null, getAlarmMessage(), "Action dialog", JOptionPane.ERROR_MESSAGE);
        return ActuatorSuccessResult.INSTANCE;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
