
/**
 * User: pizuricv
 */
package com.ai.myplugin.sensor;

import com.ai.bayes.plugins.BNSensorPlugin;
import com.ai.bayes.scenario.TestResult;
import com.ai.util.resource.TestSessionContext;
import net.xeoh.plugins.base.annotations.PluginImplementation;

import java.io.*;
import java.net.*;
import java.util.Properties;

@PluginImplementation
public class WeatherSensor extends WeatherAbstractSensor{

    @Override
    protected String getTag() {
        return WeatherAbstractSensor.WEATHER;
    }

    @Override
    protected String getSensorName() {
        return "Weather";
    }

    public static void main(String[] args){
        WeatherSensor weatherSensor = new WeatherSensor();
        weatherSensor.setProperty("city", "Gent");
        TestResult testResult = weatherSensor.execute(null);
        log.debug(testResult.getObserverState());


        weatherSensor.setProperty("city", "London");
        testResult = weatherSensor.execute(null);
        log.debug(testResult.getObserverState());

        weatherSensor.setProperty("city", "Sidney");
        testResult = weatherSensor.execute(null);
        log.debug(testResult.getObserverState());

        weatherSensor.setProperty("city", "Bangalore");
        testResult = weatherSensor.execute(null);
        log.debug(testResult.getObserverState());

        weatherSensor.setProperty("city", "Chennai");
        testResult = weatherSensor.execute(null);
        log.debug(testResult.getObserverState());

        weatherSensor.setProperty("city", "Moscow");
        testResult = weatherSensor.execute(null);
        log.debug(testResult.getObserverState());

        weatherSensor.setProperty("city", "Belgrade");
        testResult = weatherSensor.execute(null);
        log.debug(testResult.getObserverState());

        weatherSensor.setProperty("city", "Split");
        testResult = weatherSensor.execute(null);
        log.debug(testResult.getObserverState());
    }
}
