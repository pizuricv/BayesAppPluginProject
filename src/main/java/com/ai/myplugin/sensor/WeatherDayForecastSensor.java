/**
 * Created by User: veselin
 * On Date: 27/09/13
 */
package com.ai.myplugin.sensor;


import com.ai.api.PluginHeader;
import com.ai.api.SensorResult;
import net.xeoh.plugins.base.annotations.PluginImplementation;

@PluginImplementation
@PluginHeader(version = "1.0.1", author = "Veselin", category = "Weather", iconURL = "http://app.waylay.io/icons/weather_prediction.png")
public class WeatherDayForecastSensor extends WeatherAbstractSensor{
    @Override
    protected String getTag() {
        return FORECAST;
    }

    @Override
    protected String getSensorName() {
        return "WeatherDayForecastSensor";
    }

//    public static void main(String[] args){
//        WeatherDayForecastSensor weatherSensor = new WeatherDayForecastSensor();
//        weatherSensor.setProperty("city", "Gent");
//        SensorResult testResult = weatherSensor.execute(null);
//        log.debug(testResult.getObserverState());
//
//        weatherSensor.setProperty("city", "London");
//        testResult = weatherSensor.execute(null);
//        log.debug(testResult.getObserverState());
//
//        weatherSensor.setProperty("city", "Sidney");
//        testResult = weatherSensor.execute(null);
//        log.debug(testResult.getObserverState());
//
//        weatherSensor.setProperty("city", "Bangalore");
//        testResult = weatherSensor.execute(null);
//        log.debug(testResult.getObserverState());
//
//        weatherSensor.setProperty("city", "Chennai");
//        testResult = weatherSensor.execute(null);
//        log.debug(testResult.getObserverState());
//
//        weatherSensor.setProperty("city", "Moscow");
//        testResult = weatherSensor.execute(null);
//        log.debug(testResult.getObserverState());
//
//        weatherSensor.setProperty("city", "Belgrade");
//        testResult = weatherSensor.execute(null);
//        log.debug(testResult.getObserverState());
//
//        weatherSensor.setProperty("city", "Split");
//        testResult = weatherSensor.execute(null);
//        log.debug(testResult.getObserverState());
//    }
}
