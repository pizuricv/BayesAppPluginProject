package com.ai.myplugin.sensor;

import com.ai.bayes.scenario.TestResult;
import com.ai.util.resource.TestSessionContext;
import junit.framework.TestCase;

/**
 * Created by User: veselin
 * On Date: 20/03/14
 */
public class RainfallSensorTest extends TestCase{

    public void testRainfallSensorWithCity(){
        RainfallSensor rainfallSensor = new RainfallSensor();
        rainfallSensor.setProperty("location", "Gent");
        TestResult testResult = rainfallSensor.execute(new TestSessionContext(1));
        System.out.println(testResult.getObserverState());
        System.out.println(testResult.getRawData());
        assertNotNull("result not null" , testResult.getObserverState());
        assertNotNull("result not null" , testResult.getRawData());
    }

    public void testRainfallSensorWithLocation(){
        RainfallSensor rainfallSensor = new RainfallSensor();
        rainfallSensor.setProperty("longitude", 3.7174243);
        rainfallSensor.setProperty("latitude", 51.0543422);
        TestResult testResult = rainfallSensor.execute(new TestSessionContext(1));
        System.out.println(testResult.getObserverState());
        System.out.println(testResult.getRawData());
        assertNotNull("result not null" , testResult.getObserverState());
        assertNotNull("result not null" , testResult.getRawData());
    }

    public void testRainfallSensorForException(){
        RainfallSensor rainfallSensor = new RainfallSensor();

        TestResult testResult = rainfallSensor.execute(new TestSessionContext(1));
        assertFalse(testResult.isSuccess());
    }
}
