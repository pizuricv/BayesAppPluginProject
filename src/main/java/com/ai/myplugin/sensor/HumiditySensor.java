
/**
 * User: pizuricv
 */
package com.ai.myplugin.sensor;

import com.ai.bayes.scenario.TestResult;
import net.xeoh.plugins.base.annotations.PluginImplementation;

@PluginImplementation
public class HumiditySensor extends WeatherAbstractSensor{

    @Override
    protected String getTag() {
        return WeatherAbstractSensor.HUMIDITY;
    }

    @Override
    protected String getSensorName() {
        return "Humidity";
    }

    public static void main(String[] args){
        HumiditySensor weatherSensor = new HumiditySensor();
        weatherSensor.setProperty("city", "Gent");
        TestResult testResult = weatherSensor.execute(null);
        System.out.println(testResult.getObserverState());

        weatherSensor.setProperty("city", "London");
        testResult = weatherSensor.execute(null);
        System.out.println(testResult.getObserverState());

        weatherSensor.setProperty("city", "Sidney");
        testResult = weatherSensor.execute(null);
        System.out.println(testResult.getObserverState());

        weatherSensor.setProperty("city", "Bangalore");
        testResult = weatherSensor.execute(null);
        System.out.println(testResult.getObserverState());

        weatherSensor.setProperty("city", "Chennai");
        testResult = weatherSensor.execute(null);
        System.out.println(testResult.getObserverState());

        weatherSensor.setProperty("city", "Moscow");
        testResult = weatherSensor.execute(null);
        System.out.println(testResult.getObserverState());

        weatherSensor.setProperty("city", "Belgrade");
        testResult = weatherSensor.execute(null);
        System.out.println(testResult.getObserverState());

        weatherSensor.setProperty("city", "Split");
        testResult = weatherSensor.execute(null);
        System.out.println(testResult.getObserverState());
    }
}
