package com.ai.myplugin.sensor;

import com.ai.api.PluginHeader;
import net.xeoh.plugins.base.annotations.PluginImplementation;

import java.util.Map;

@PluginImplementation
@PluginHeader(version = "1.0.1", author = "Veselin", category = "Stock", iconURL = "http://app.waylay.io/icons/stocks-falling.png")
public class StockLowSensor extends StockAbstractSensor {

    @Override
    protected String getSensorName() {
        return "StockLow";
    }

    @Override
    public String getDescription() {
        return "Stock exchange sensor, low price value";
    }

    @Override
    protected String getObserverState(Map<String, Double> results, Double threshold) {
        if(results.get(LOW) == null){
            return null;
        }
        if(results.get(LOW) < threshold)
            return STATE_BELOW;
        return STATE_ABOVE;
    }
}