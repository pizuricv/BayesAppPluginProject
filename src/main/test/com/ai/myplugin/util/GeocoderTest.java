package com.ai.myplugin.util;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Test;

import static org.junit.Assert.*;

public class GeocoderTest {

    @Test
    public void testGetLongitudeLatitudeForAddress() throws Exception {
        LatLng gent = Geocoder.getLongitudeLatitudeForAddress("Krekelstraat 60, 9052 Gent, Belgium");
        assertEquals(new LatLng(50.9968091, 3.6862785), gent);
    }

    @Test
    public void testReverse(){
        JSONObject json = Geocoder.reverseLookupAddress(45.262231, 19.851858);
        System.out.println(json.toJSONString());
        assertEquals("Saudi Arabia", json.get("country"));
    }
}