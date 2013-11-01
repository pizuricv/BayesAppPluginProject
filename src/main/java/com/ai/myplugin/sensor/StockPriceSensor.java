/**
 * User: pizuricv
 * Date: 6/4/13
 */

package com.ai.myplugin.sensor;

import com.ai.bayes.scenario.TestResult;
import net.xeoh.plugins.base.annotations.PluginImplementation;

import java.util.*;

@PluginImplementation
public class StockPriceSensor extends StockAbstractSensor {

    @Override
    protected String getTag() {
        return StockAbstractSensor.PRICE;
    }

    @Override
    protected String getSensorName() {
        return "StockPrice";
    }

    public static void main(String[] args){
        StockPriceSensor stockSensor = new StockPriceSensor();
        stockSensor.setProperty(STOCK, "MSFT");
        stockSensor.setProperty(THRESHOLD, "36");
        log.debug(Arrays.toString(stockSensor.getSupportedStates()));
        log.debug(stockSensor.execute(null).getObserverState());


        stockSensor.setProperty(STOCK, "GOOG");
        stockSensor.setProperty(THRESHOLD, "800.0");
        TestResult testResult = stockSensor.execute(null);
        log.debug(testResult.getObserverState());
        log.debug(testResult.getRawData());
    }
}
