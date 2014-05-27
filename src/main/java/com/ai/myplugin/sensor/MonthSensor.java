package com.ai.myplugin.sensor;

import com.ai.api.PluginHeader;
import net.xeoh.plugins.base.annotations.PluginImplementation;


@PluginImplementation
@PluginHeader(version = "1.0.1", author = "Veselin", category = "Time", iconURL = "http://app.waylay.io/icons/month.png")
public class MonthSensor extends TimeAbstractSensor {
    @Override
    protected String getTag() {
        return TimeAbstractSensor.MONTH;
    }

    @Override
    protected String getSensorName() {
        return "Month";
    }
}