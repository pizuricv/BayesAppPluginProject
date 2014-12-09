package com.ai.myplugin.sensor;

import com.ai.api.SensorResult;
import com.ai.api.SessionContext;
import com.ai.myplugin.TestSessionContext;
import com.ai.myplugin.services.RainResult;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Collections;

public class RainfallSensorTest{

    @Test
    public void testRainfallSensorWithCity(){
        RainfallSensor rainfallSensor = new RainfallSensor();
        rainfallSensor.setProperty(RainfallSensor.LOCATION, "Gent");

        SensorResult testResult = rainfallSensor.execute(new TestSessionContext());

        assertTrue(testResult.errorMessage(), testResult.isSuccess());
        assertNotNull("state is null", testResult.getObserverState());
        assertNotNull("raw data is null" , testResult.getRawData());
    }

    @Test
    public void testRainfallSensorWithLocation(){
        RainfallSensor rainfallSensor = new RainfallSensor();
        rainfallSensor.setProperty("longitude", 3.7174243);
        rainfallSensor.setProperty("latitude", 51.0543422);

        SensorResult testResult = rainfallSensor.execute(new TestSessionContext());

        assertTrue(testResult.errorMessage(), testResult.isSuccess());
        assertNotNull("state is null" , testResult.getObserverState());
        assertNotNull("raw data is null" , testResult.getRawData());
    }

    @Test
    public void testRainfallSensorForException(){
        RainfallSensor rainfallSensor = new RainfallSensor();

        SensorResult testResult = rainfallSensor.execute(new TestSessionContext());

        assertFalse(testResult.isSuccess());
        assertEquals("error in getting the location: latitude, longitude and/or location not configured", testResult.errorMessage());
    }

    @Test
    public void testEvaluation(){
        RainResult result = new RainResult(10, 60, 40, Collections.emptyList());
        assertEquals(RainfallSensor.STATE_RAIN, RainfallSensor.evaluate(result));
    }
}
