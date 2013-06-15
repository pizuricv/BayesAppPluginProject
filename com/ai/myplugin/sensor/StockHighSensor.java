package com.ai.myplugin.sensor;

import net.xeoh.plugins.base.annotations.PluginImplementation;

@PluginImplementation
public class StockHighSensor extends StockAbstractSensor {
    @Override
    protected String getTag() {
        return StockAbstractSensor.HIGH;
    }

    @Override
    protected String getSensorName() {
        return "StockHighSensor";
    }
}
