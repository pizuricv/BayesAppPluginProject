package com.ai.myplugin.sensor;

import net.xeoh.plugins.base.annotations.PluginImplementation;

@PluginImplementation
public class StockLowSensor extends StockAbstractSensor {
    @Override
    protected String getTag() {
        return StockAbstractSensor.LOW;
    }

    @Override
    protected String getSensorName() {
        return "StockLowSensor";
    }
}