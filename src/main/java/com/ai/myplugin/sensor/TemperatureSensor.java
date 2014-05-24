/**
 * User: pizuricv
 */

package com.ai.myplugin.sensor;

import com.ai.api.SensorResult;
import net.xeoh.plugins.base.annotations.PluginImplementation;


@PluginImplementation
public class TemperatureSensor extends WeatherAbstractSensor{


    @Override
    protected String getTag() {
        return WeatherAbstractSensor.TEMP;
    }

    @Override
    protected String getSensorName() {
        return "Temperature";
    }

    public static void main(String[] args){
        TemperatureSensor weatherSensor = new TemperatureSensor();
        weatherSensor.setProperty("city", "London");
        SensorResult testResult = weatherSensor.execute(null);
        log.info(testResult.getObserverState());

        weatherSensor.setProperty("city", "Bangalore");
        testResult = weatherSensor.execute(null);
        log.info(testResult.getObserverState());

        weatherSensor.setProperty("city", "Chennai");
        testResult = weatherSensor.execute(null);
        log.info(testResult.getObserverState());

        weatherSensor.setProperty("city", "Moscow");
        testResult = weatherSensor.execute(null);
        log.info(testResult.getObserverState());

        weatherSensor.setProperty("city", "Belgrade");
        testResult = weatherSensor.execute(null);
        log.info(testResult.getObserverState());

        weatherSensor.setProperty("city", "Split");
        testResult = weatherSensor.execute(null);
        log.info(testResult.getObserverState());
        log.info(testResult.getRawData());
    }
}
