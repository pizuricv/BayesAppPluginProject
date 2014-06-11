package com.ai.myplugin.sensor;

import com.ai.api.PluginHeader;
import net.xeoh.plugins.base.annotations.PluginImplementation;

@PluginImplementation
@PluginHeader(version = "1.0.1", author = "Veselin", category = "Stock", iconURL = "http://app.waylay.io/icons/stocks-falling.png")
public class StockLowSensor extends StockAbstractSensor {
    @Override
    protected String getTag() {
        return StockAbstractSensor.LOW;
    }

    @Override
    protected String getSensorName() {
        return "StockLow";
    }

    @Override
    public String getDescription() {
        return "Stock exchange sensor, low price value";
    }
}