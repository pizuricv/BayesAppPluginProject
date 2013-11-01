/**
 * Created by User: veselin
 * On Date: 27/09/13
 */
package com.ai.myplugin.sensor;


import net.xeoh.plugins.base.annotations.PluginImplementation;


import com.ai.bayes.scenario.TestResult;
import net.xeoh.plugins.base.annotations.PluginImplementation;

@PluginImplementation

public class WeatherDayForecastSensor extends WeatherAbstractSensor{
    @Override
    protected String getTag() {
        return FORECAST;
    }

    @Override
    protected String getSensorName() {
        return "WeatherDayForecastSensor";
    }

    public static void main(String[] args){
        WeatherDayForecastSensor weatherSensor = new WeatherDayForecastSensor();
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
