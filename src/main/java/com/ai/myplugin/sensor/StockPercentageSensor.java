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
    protected String getTag() {
        return StockAbstractSensor.PERCENT;
    }

    @Override
    protected String getSensorName() {
        return "StockPercentage";
    }

    @Override
    public String getDescription() {
        return "Stock exchange sensor, percentage price value";
    }

//    public static void main(String[] args){
//        StockPercentageSensor stockSensor = new StockPercentageSensor();
//        stockSensor.setProperty(STOCK, "MSFT");
//        stockSensor.setProperty(THRESHOLD, "1");
//        log.debug(Arrays.toString(stockSensor.getSupportedStates()));
//        log.debug(stockSensor.execute(null).getObserverState());
//
//
//        stockSensor.setProperty(STOCK, "GOOG");
//        stockSensor.setProperty(THRESHOLD, "2");
//        log.debug(stockSensor.execute(null).getObserverState());
//
//        stockSensor.setProperty(STOCK, "BAR.BR");
//        stockSensor.setProperty(THRESHOLD, "-1.0");
//        log.debug(stockSensor.execute(null).getObserverState());
//    }
}
