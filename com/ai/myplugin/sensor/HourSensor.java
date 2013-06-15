package com.ai.myplugin.sensor;

import net.xeoh.plugins.base.annotations.PluginImplementation;


@PluginImplementation
public class HourSensor extends TimeAbstractSensor {
    @Override
    protected String getTag() {
        return TimeAbstractSensor.HOUR;
    }

    @Override
    protected String getSensorName() {
        return "HourSensor";
    }
}
