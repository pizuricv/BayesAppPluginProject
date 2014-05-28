package com.ai.myplugin.sensor;

import com.ai.api.SensorResult;
import org.junit.Test;

import static org.junit.Assert.*;

public class HumiditySensorTest {

    @Test
    public void testExecute(){
        HumiditySensor weatherSensor = new HumiditySensor();
        weatherSensor.setProperty("city", "Gent");
        SensorResult testResult = weatherSensor.execute(null);
        System.out.println(testResult.getObserverState());

//        weatherSensor.setProperty("city", "London");
//        testResult = weatherSensor.execute(null);
//        System.out.println(testResult.getObserverState());
//
//        weatherSensor.setProperty("city", "Sidney");
//        testResult = weatherSensor.execute(null);
//        System.out.println(testResult.getObserverState());
//
//        weatherSensor.setProperty("city", "Bangalore");
//        testResult = weatherSensor.execute(null);
//        System.out.println(testResult.getObserverState());
//
//        weatherSensor.setProperty("city", "Chennai");
//        testResult = weatherSensor.execute(null);
//        System.out.println(testResult.getObserverState());
//
//        weatherSensor.setProperty("city", "Moscow");
//        testResult = weatherSensor.execute(null);
//        System.out.println(testResult.getObserverState());
//
//        weatherSensor.setProperty("city", "Belgrade");
//        testResult = weatherSensor.execute(null);
//        System.out.println(testResult.getObserverState());
//
//        weatherSensor.setProperty("city", "Split");
//        testResult = weatherSensor.execute(null);
//        System.out.println(testResult.getObserverState());
    }

}