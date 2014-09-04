/**
 * User: pizuricv
 * Date: 6/4/13
 */

package com.ai.myplugin.sensor;

import com.ai.api.PluginHeader;
import net.xeoh.plugins.base.annotations.PluginImplementation;

import java.util.*;

@PluginImplementation
@PluginHeader(version = "1.0.1", author = "Veselin", category = "Stock", iconURL = "http://app.waylay.io/icons/mvolume.png")
public class StockVolumeSensor extends StockAbstractSensor {

    @Override
    protected String getSensorName() {
        return "StockVolume";
    }

    @Override
    public String getDescription() {
        return "Stock exchange sensor, volume price value";
    }

    @Override
    protected String getObserverState(Map<String, Double> results, Double threshold) {
        if(!results.containsKey(VOLUME)){
            return null;
        }
        if(results.get(VOLUME) < threshold)
            return STATE_BELOW;
        return STATE_ABOVE;
    }
}
