/**
 * Created by User: veselin
 * On Date: 20/03/14
 */

package com.ai.myplugin.sensor;

import com.ai.api.SensorResult;
import com.ai.api.SessionContext;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Collections;
import java.util.Optional;

public class RainfallSensorTest{

    @Test
    public void testRainfallSensorWithCity(){
        RainfallSensor rainfallSensor = new RainfallSensor();
        rainfallSensor.setProperty("location", "Gent");
        SensorResult testResult = rainfallSensor.execute(new SessionContext(1));
        System.out.println(testResult.getObserverState());
        System.out.println(testResult.getRawData());
        assertNotNull("state is null", testResult.getObserverState());
        assertNotNull("raw data is null" , testResult.getRawData());
    }

    @Test
    public void testRainfallSensorWithLocation(){
        RainfallSensor rainfallSensor = new RainfallSensor();
        rainfallSensor.setProperty("longitude", 3.7174243);
        rainfallSensor.setProperty("latitude", 51.0543422);
        SensorResult testResult = rainfallSensor.execute(new SessionContext(1));
        assertNotNull("state is null" , testResult.getObserverState());
        assertNotNull("raw data is null" , testResult.getRawData());
    }

    @Test
    public void testRainfallSensorForException(){
        RainfallSensor rainfallSensor = new RainfallSensor();
        SensorResult testResult = rainfallSensor.execute(new SessionContext(1));
        assertFalse(testResult.isSuccess());
    }

    @Test
    public void testParseResponse(){
        RainfallSensor rainfallSensor = new RainfallSensor();
        Optional<RainfallSensor.RainResult> resultOpt = rainfallSensor.parseResponse("000|10:20 000|10:25 000|10:30 000|10:35 000|10:40 000|10:45 000|10:50 000|10:55 000|11:00 000|11:05 000|11:10 000|11:15 000|11:20 000|11:25 000|11:30 000|11:35 000|11:40 000|11:45 000|11:50 000|11:55 000|12:00 000|12:05 000|12:10 000|12:15 000|12:20");
        RainfallSensor.RainResult result = resultOpt.get();
        assertEquals(0.0, result.avg, 0.0);
        assertEquals(0.0, result.min, 0.0);
        assertEquals(0.0, result.max, 0.0);
        assertEquals(1, result.results.size());
    }

    @Test
    public void testParseResponseNoData(){
        RainfallSensor rainfallSensor = new RainfallSensor();
        // this is returned sometimes
        Optional<RainfallSensor.RainResult> result =  rainfallSensor.parseResponse("|10:15|10:20|10:25|10:30|10:35|10:40|10:45|10:50|10:55|11:00|11:05|11:10|11:15|11:20|11:25|11:30|11:35|11:40|11:45|11:50|11:55|12:00|12:05|12:10|12:15");
        assertFalse(result.isPresent());
    }

    @Test
    public void testEvaluation(){
        RainfallSensor.RainResult result = new RainfallSensor.RainResult(10, 60, 40, Collections.emptyList());
        assertEquals(RainfallSensor.STATE_RAIN, result.evaluate());
    }
}
