package com.ai.myplugin.sensor;

import com.ai.api.DataType;
import com.ai.api.PluginHeader;
import com.ai.api.PropertyType;
import net.xeoh.plugins.base.annotations.PluginImplementation;

import java.util.HashMap;
import java.util.Map;


@PluginImplementation
@PluginHeader(version = "1.0.1", author = "Veselin", category = "Time", iconURL = "http://app.waylay.io/icons/calendar.png")
public class DateSensor extends TimeAbstractSensor {
    @Override
    protected String getTag() {
        return TimeAbstractSensor.DATE;
    }

    @Override
    protected String getSensorName() {
        return "Date";
    }

    @Override
    public String getDescription() {
        return "Returns date";
    }

    @Override
    public Map<String, PropertyType> getRequiredProperties() {
        Map<String, PropertyType> map = new HashMap<>();
        map.put(TIME_ZONE, new PropertyType(DataType.STRING, true, false));
        map.put(DATE, new PropertyType(DataType.DATE, true, false));
        return map;
    }
}
