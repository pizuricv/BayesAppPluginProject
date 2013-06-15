package com.ai.myplugin.sensor;

import net.xeoh.plugins.base.annotations.PluginImplementation;

@PluginImplementation
public class StockLowSensor extends StockSensor{
    @Override
    protected String getTag() {
        return StockSensor.LOW;
    }

    @Override
    protected String getSensorName() {
        return "StockLowSensor";
    }
}