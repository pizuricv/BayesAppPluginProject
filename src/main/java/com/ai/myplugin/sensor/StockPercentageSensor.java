/**
 * User: pizuricv
 * Date: 6/4/13
 */

package com.ai.myplugin.sensor;

import com.ai.api.PluginHeader;
import net.xeoh.plugins.base.annotations.PluginImplementation;

import java.util.*;

@PluginImplementation
@PluginHeader(version = "1.0.1", author = "Veselin", category = "Stock", iconURL = "http://app.waylay.io/icons/mpercentage.png")
public class StockPercentageSensor extends StockAbstractSensor {

    @Override
    protected String getSensorName() {
        return "StockPercentage";
    }

    @Override
    public String getDescription() {
        return "Stock exchange sensor, percentage price value";
    }

    @Override
    protected String getObserverState(Map<String, Double> results, Double threshold) {
        if(results.get(PERCENT) < threshold)
            return STATE_BELOW;
        return STATE_ABOVE;
    }
}
