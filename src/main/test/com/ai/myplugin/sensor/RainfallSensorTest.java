/**
 * Created by User: veselin
 * On Date: 20/03/14
 */

package com.ai.myplugin.sensor;

import com.ai.api.SensorResult;
import com.ai.api.SessionContext;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Collections;
import java.util.Optional;

public class RainfallSensorTest{

    @Test
    public void testRainfallSensorWithCity(){
        RainfallSensor rainfallSensor = new RainfallSensor();
        rainfallSensor.setProperty(RainfallSensor.LOCATION, "Gent");
        SensorResult testResult = rainfallSensor.execute(new SessionContext(1));
        assertNotNull("state is null", testResult.getObserverState());
        assertNotNull("raw data is null" , testResult.getRawData());
    }

    @Test
    @Ignore
    public void testRainfallSensorWithLocation(){
        RainfallSensor rainfallSensor = new RainfallSensor();
        rainfallSensor.setProperty("longitude", 3.7174243);
        rainfallSensor.setProperty("latitude", 51.0543422);
        SensorResult testResult = rainfallSensor.execute(new SessionContext(1));
        assertNotNull("state is null" , testResult.getObserverState());
        assertNotNull("raw data is null" , testResult.getRawData());
    }

    @Test
    @Ignore
    public void testRainfallSensorForException(){
        RainfallSensor rainfallSensor = new RainfallSensor();
        SensorResult testResult = rainfallSensor.execute(new SessionContext(1));
        assertFalse(testResult.isSuccess());
    }

    @Test
    public void testParseResponse(){
        RainfallSensor rainfallSensor = new RainfallSensor();
        // TODO get this from a classpath resource file
        Optional<RainfallSensor.RainResult> resultOpt = rainfallSensor.parseResponse("000|10:20\n000|10:25\n000|10:30\n000|10:35\n000|10:40\n000|10:45\n000|10:50\n000|10:55\n000|11:00\n000|11:05\n000|11:10\n000|11:15\n000|11:20\n000|11:25\n000|11:30\n000|11:35\n000|11:40\n000|11:45\n000|11:50\n000|11:55\n000|12:00\n000|12:05\n000|12:10\n000|12:15\n000|12:20\n");
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
        // TODO get this from a classpath resource file
        Optional<RainfallSensor.RainResult> result =  rainfallSensor.parseResponse("|10:15\n|10:20\n|10:25\n|10:30\n|10:35\n|10:40\n|10:45\n|10:50\n|10:55\n|11:00\n|11:05\n|11:10\n|11:15\n|11:20\n|11:25\n|11:30\n|11:35\n|11:40\n|11:45\n|11:50\n|11:55\n|12:00\n|12:05\n|12:10\n|12:15\n");
        assertFalse(result.isPresent());
    }

    @Test
    @Ignore
    public void testEvaluation(){
        RainfallSensor.RainResult result = new RainfallSensor.RainResult(10, 60, 40, Collections.emptyList());
        assertEquals(RainfallSensor.STATE_RAIN, result.evaluate());
    }
}
