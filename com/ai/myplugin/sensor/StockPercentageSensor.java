/**
 * User: pizuricv
 * Date: 6/4/13
 */

package com.ai.myplugin.sensor;

import net.xeoh.plugins.base.annotations.PluginImplementation;

import java.util.*;

@PluginImplementation
public class StockPercentageSensor extends StockAbstractSensor {

    @Override
    protected String getTag() {
        return StockAbstractSensor.PERCENT;
    }

    @Override
    protected String getSensorName() {
        return "StockPercentage";
    }

    public static void main(String[] args){
        StockPercentageSensor stockSensor = new StockPercentageSensor();
        stockSensor.setProperty(STOCK, "MSFT");
        stockSensor.setProperty(THRESHOLD, "1");
        System.out.println(Arrays.toString(stockSensor.getSupportedStates()));
        System.out.println(stockSensor.execute(null).getObserverState());


        stockSensor.setProperty(STOCK, "GOOG");
        stockSensor.setProperty(THRESHOLD, "2");
        System.out.println(stockSensor.execute(null).getObserverState());

        stockSensor.setProperty(STOCK, "BAR.BR");
        stockSensor.setProperty(THRESHOLD, "-1.0");
        System.out.println(stockSensor.execute(null).getObserverState());
    }
}
