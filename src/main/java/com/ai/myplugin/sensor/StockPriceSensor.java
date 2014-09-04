/**
 * User: pizuricv
 * Date: 6/4/13
 */

package com.ai.myplugin.sensor;

import com.ai.api.PluginHeader;
import com.ai.api.SensorResult;
import net.xeoh.plugins.base.annotations.PluginImplementation;

import java.util.*;

@PluginImplementation
@PluginHeader(version = "1.0.1", author = "Veselin", category = "Stock", iconURL = "http://app.waylay.io/icons/stock.png")
public class StockPriceSensor extends StockAbstractSensor {

    @Override
    protected String getSensorName() {
        return "StockPrice";
    }

    @Override
    public String getDescription() {
        return "Stock exchange sensor, stock price value";
    }

    @Override
    protected String getObserverState(Map<String, Double> results, Double threshold) {
        if(results.get(PRICE) == null){
            return null;
        }
        if(results.get(PRICE) < threshold) {
            return STATE_BELOW;
        }else {
            return STATE_ABOVE;
        }
    }
}
