/**
 * User: pizuricv
 * Date: 6/4/13
 */

package com.ai.myplugin.sensor;

import com.ai.bayes.plugins.BNSensorPlugin;
import com.ai.bayes.scenario.TestResult;
import com.ai.util.resource.TestSessionContext;
import net.xeoh.plugins.base.annotations.PluginImplementation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@PluginImplementation
public class StockPriceSensor extends StockSensor{

    @Override
    protected String getTag() {
        return StockSensor.PRICE;
    }

    @Override
    protected String getSensorName() {
        return "StockPrice";
    }

    public static void main(String[] args){
        StockPriceSensor stockSensor = new StockPriceSensor();
        stockSensor.setProperty(STOCK, "MSFT");
        stockSensor.setProperty(THRESHOLD, "36");
        System.out.println(Arrays.toString(stockSensor.getSupportedStates()));
        System.out.println(stockSensor.execute(null).getObserverState());


        stockSensor.setProperty(STOCK, "GOOG");
        stockSensor.setProperty(THRESHOLD, "800.0");
        System.out.println(stockSensor.execute(null).getObserverState());
    }
}
