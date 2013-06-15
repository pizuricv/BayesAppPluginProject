package com.ai.myplugin.sensor;

import net.xeoh.plugins.base.annotations.PluginImplementation;


@PluginImplementation
public class WeekSensor extends TimeAbstractSensor {
    @Override
    protected String getTag() {
        return TimeAbstractSensor.WEEK;
    }

    @Override
    protected String getSensorName() {
        return "Week";
    }
}