package com.ai.myplugin.sensor;

import net.xeoh.plugins.base.annotations.PluginImplementation;


@PluginImplementation
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