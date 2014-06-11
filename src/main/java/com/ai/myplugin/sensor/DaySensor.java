package com.ai.myplugin.sensor;

import com.ai.api.PluginHeader;
import net.xeoh.plugins.base.annotations.PluginImplementation;


@PluginImplementation
@PluginHeader(version = "1.0.1", author = "Veselin", category = "Time", iconURL = "http://app.waylay.io/icons/day.png")
public class DaySensor extends TimeAbstractSensor {
    @Override
    protected String getTag() {
        return TimeAbstractSensor.DAY;
    }

    @Override
    protected String getSensorName() {
        return "Day";
    }

    @Override
    public String getDescription() {
        return "Returns day in a month";
    }

}
