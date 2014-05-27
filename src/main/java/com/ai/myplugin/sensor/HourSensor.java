package com.ai.myplugin.sensor;

import com.ai.api.PluginHeader;
import net.xeoh.plugins.base.annotations.PluginImplementation;


@PluginImplementation
@PluginHeader(version = "1.0.1", author = "Veselin", category = "Time", iconURL = "http://app.waylay.io/icons/clock.png")
public class HourSensor extends TimeAbstractSensor {
    @Override
    protected String getTag() {
        return TimeAbstractSensor.HOUR;
    }

    @Override
    protected String getSensorName() {
        return "Hour";
    }
}
