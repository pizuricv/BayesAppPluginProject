/**
 * User: pizuricv
 * Date: 6/4/13
 */

package com.ai.myplugin.sensor;

import net.xeoh.plugins.base.annotations.PluginImplementation;

import java.util.*;

@PluginImplementation
public class StockMovingAverageSensor extends StockAbstractSensor {


    @Override
    protected String getTag() {
        return StockAbstractSensor.MOVING_AVERAGE;
    }

    @Override
    protected String getSensorName() {
        return "StockMovingAverage";
    }

    public static void main(String[] args){
        StockMovingAverageSensor stockSensor = new StockMovingAverageSensor();
        stockSensor.setProperty(StockAbstractSensor.STOCK, "MSFT");
        stockSensor.setProperty(StockAbstractSensor.THRESHOLD, "36");
        log.debug(Arrays.toString(stockSensor.getSupportedStates()));
        log.debug(stockSensor.execute(null).getObserverState());


        stockSensor.setProperty(STOCK, "GOOG");
        stockSensor.setProperty(THRESHOLD, "800.0");
        log.debug(stockSensor.execute(null).getObserverState());

        stockSensor.setProperty(STOCK, "BAR.BR");
        stockSensor.setProperty(THRESHOLD, "-1.0");
        log.debug(stockSensor.execute(null).getObserverState());
    }
}
