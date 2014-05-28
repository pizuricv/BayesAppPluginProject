package com.ai.myplugin.sensor;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class StockMovingAverageSensorTest {

    @Test
    public void testExecute(){
        StockMovingAverageSensor stockSensor = new StockMovingAverageSensor();
        stockSensor.setProperty(StockAbstractSensor.STOCK, "MSFT");
        stockSensor.setProperty(StockAbstractSensor.THRESHOLD, "36");
        System.out.println(stockSensor.getSupportedStates());
        System.out.println(stockSensor.execute(null).getObserverState());


        stockSensor.setProperty(StockAbstractSensor.STOCK, "GOOG");
        stockSensor.setProperty(StockAbstractSensor.THRESHOLD, "800.0");
        System.out.println(stockSensor.execute(null).getObserverState());

        stockSensor.setProperty(StockAbstractSensor.STOCK, "BAR.BR");
        stockSensor.setProperty(StockAbstractSensor.THRESHOLD, "-1.0");
        System.out.println(stockSensor.execute(null).getObserverState());
    }

}