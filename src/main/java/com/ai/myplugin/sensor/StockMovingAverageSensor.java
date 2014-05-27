/**
 * User: pizuricv
 * Date: 6/4/13
 */

package com.ai.myplugin.sensor;

import com.ai.api.PluginHeader;
import net.xeoh.plugins.base.annotations.PluginImplementation;

import java.util.*;

@PluginImplementation
@PluginHeader(version = "1.0.1", author = "Veselin", category = "Stock", iconURL = "http://app.waylay.io/icons/maverage.png")
public class StockMovingAverageSensor extends StockAbstractSensor {


    @Override
    protected String getTag() {
        return StockAbstractSensor.MOVING_AVERAGE;
    }

    @Override
    protected String getSensorName() {
        return "StockMovingAverage";
    }

}
