package com.ai.myplugin.sensor;

import net.xeoh.plugins.base.annotations.PluginImplementation;

@PluginImplementation
public class StockHighSensor extends StockSensor{
    @Override
    protected String getTag() {
        return StockSensor.HIGH;
    }

    @Override
    protected String getSensorName() {
        return "StockHighSensor";
    }
}
