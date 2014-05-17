/**
 * Created by User: veselin
 * On Date: 20/03/14
 */

package com.ai.myplugin.sensor;

import com.ai.api.SensorResult;
import com.ai.api.SessionContext;
import junit.framework.TestCase;

public class RainfallSensorTest extends TestCase{

    public void testRainfallSensorWithCity(){
        RainfallSensor rainfallSensor = new RainfallSensor();
        rainfallSensor.setProperty("location", "Gent");
        SensorResult testResult = rainfallSensor.execute(new SessionContext(1));
        System.out.println(testResult.getObserverState());
        System.out.println(testResult.getRawData());
        assertNotNull("result not null" , testResult.getObserverState());
        assertNotNull("result not null" , testResult.getRawData());
    }

    public void testRainfallSensorWithLocation(){
        RainfallSensor rainfallSensor = new RainfallSensor();
        rainfallSensor.setProperty("longitude", 3.7174243);
        rainfallSensor.setProperty("latitude", 51.0543422);
        SensorResult testResult = rainfallSensor.execute(new SessionContext(1));
        System.out.println(testResult.getObserverState());
        System.out.println(testResult.getRawData());
        assertNotNull("result not null" , testResult.getObserverState());
        assertNotNull("result not null" , testResult.getRawData());
    }

    public void testRainfallSensorForException(){
        RainfallSensor rainfallSensor = new RainfallSensor();

        SensorResult testResult = rainfallSensor.execute(new SessionContext(1));
        assertFalse(testResult.isSuccess());
    }
}
