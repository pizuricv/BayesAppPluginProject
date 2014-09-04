package com.ai.myplugin.sensor;

import com.ai.api.PluginHeader;
import net.xeoh.plugins.base.annotations.PluginImplementation;

import java.util.Map;

@PluginImplementation
@PluginHeader(version = "1.0.1", author = "Veselin", category = "Stock", iconURL = "http://app.waylay.io/icons/stocks-rising.png")
public class StockHighSensor extends StockAbstractSensor {

    @Override
    protected String getSensorName() {
        return "StockHigh";
    }

    @Override
    public String getDescription() {
        return "Stock exchange sensor, high price value";
    }

    @Override
    protected String getObserverState(Map<String, Double> results, Double threshold) {
        if(results.get(HIGH) == null){
            return null;
        }
        if(results.get(HIGH) < threshold)
            return STATE_BELOW;
        return STATE_ABOVE;
    }
}
