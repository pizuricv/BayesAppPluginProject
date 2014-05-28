package com.ai.myplugin.services;

import com.ai.myplugin.sensor.RainfallSensor;
import com.ai.myplugin.util.LatLng;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class BuienradarServiceTest {

    @Test
    public void testFetch() throws Exception {
        BuienradarService service = new BuienradarService();
        Optional<RainResult> resultOpt = service.fetch(new LatLng(51.21283 , 3.22383));
        assertTrue(resultOpt.isPresent());
        RainResult result = resultOpt.get();
        assertEquals(25, result.results.size());
        //System.out.println(result);
    }

    @Test
    public void testParseResponse(){
        BuienradarService service = new BuienradarService();
        // TODO get this from a classpath resource file
        Optional<RainResult> resultOpt = service.parseResponse("000|10:20\n000|10:25\n000|10:30\n000|10:35\n000|10:40\n000|10:45\n000|10:50\n000|10:55\n000|11:00\n000|11:05\n000|11:10\n000|11:15\n000|11:20\n000|11:25\n000|11:30\n000|11:35\n000|11:40\n000|11:45\n000|11:50\n000|11:55\n000|12:00\n000|12:05\n000|12:10\n000|12:15\n000|12:20\n");
        assertTrue(resultOpt.isPresent());
        RainResult result = resultOpt.get();
        assertEquals(0.0, result.avg, 0.0);
        assertEquals(0.0, result.min, 0.0);
        assertEquals(0.0, result.max, 0.0);
        assertEquals(25, result.results.size());
    }

    @Test
    public void testParseResponseNoData(){
        BuienradarService service = new BuienradarService();
        // this is returned sometimes
        // TODO get this from a classpath resource file
        Optional<RainResult> resultOpt =  service.parseResponse("|10:15\n|10:20\n|10:25\n|10:30\n|10:35\n|10:40\n|10:45\n|10:50\n|10:55\n|11:00\n|11:05\n|11:10\n|11:15\n|11:20\n|11:25\n|11:30\n|11:35\n|11:40\n|11:45\n|11:50\n|11:55\n|12:00\n|12:05\n|12:10\n|12:15\n");
        assertTrue(resultOpt.isPresent());
        RainResult result = resultOpt.get();
        assertEquals(25, result.results.size());
    }
}