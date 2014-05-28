package com.ai.myplugin.services;

import com.ai.myplugin.util.LatLng;
import com.ai.myplugin.util.io.IOUtil;
import org.junit.Test;

import java.io.IOException;
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
    public void testParseResponse() throws IOException{
        BuienradarService service = new BuienradarService();
        String fileContents = IOUtil.readFromClasspath(getClass(), "buienradar_norain.txt");
        Optional<RainResult> resultOpt = service.parseResponse(fileContents);
        assertTrue(resultOpt.isPresent());
        RainResult result = resultOpt.get();
        assertEquals(0.0, result.avg, 0.0);
        assertEquals(0.0, result.min, 0.0);
        assertEquals(0.0, result.max, 0.0);
        assertEquals(25, result.results.size());
    }

    @Test
    public void testParseResponseNoData() throws IOException{
        BuienradarService service = new BuienradarService();
        // this is returned sometimes
        String fileContents = IOUtil.readFromClasspath(getClass(), "buienradar_nodata.txt");
        Optional<RainResult> resultOpt = service.parseResponse(fileContents);
        assertTrue(resultOpt.isPresent());
        RainResult result = resultOpt.get();
        assertEquals(25, result.results.size());
    }
}