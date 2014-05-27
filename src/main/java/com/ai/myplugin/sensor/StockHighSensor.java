package com.ai.myplugin.sensor;

import com.ai.api.PluginHeader;
import net.xeoh.plugins.base.annotations.PluginImplementation;

@PluginImplementation
@PluginHeader(version = "1.0.1", author = "Veselin", category = "Stock", iconURL = "http://app.waylay.io/icons/stocks-rising.png")
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
