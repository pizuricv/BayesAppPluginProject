/**
 * User: pizuricv
 * Date: 6/4/13
 */

package com.ai.myplugin.sensor;

import net.xeoh.plugins.base.annotations.PluginImplementation;

import java.util.*;

@PluginImplementation
public class StockVolumeSensor extends StockAbstractSensor {

    @Override
    protected String getTag() {
        return StockAbstractSensor.VOLUME;
    }

    @Override
    protected String getSensorName() {
        return "StockVolume";
    }

    public static void main(String[] args){
        StockVolumeSensor stockSensor = new StockVolumeSensor();
        stockSensor.setProperty(STOCK, "MSFT");
        stockSensor.setProperty(THRESHOLD, "36");
        System.out.println(Arrays.toString(stockSensor.getSupportedStates()));
        System.out.println(stockSensor.execute(null).getObserverState());

        stockSensor.setProperty(STOCK, "GOOG");
        stockSensor.setProperty(THRESHOLD, "800.0");
        System.out.println(stockSensor.execute(null).getObserverState());

        stockSensor.setProperty(STOCK, "BAR.BR");
        stockSensor.setProperty(THRESHOLD, "-1.0");
        System.out.println(stockSensor.execute(null).getObserverState());
    }
}
