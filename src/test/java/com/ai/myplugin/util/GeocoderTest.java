package com.ai.myplugin.util;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

public class GeocoderTest {

    @Test
    public void testGetLongitudeLatitudeForAddress() throws Exception {
        LatLng gent = Geocoder.getLongitudeLatitudeForAddress("Krekelstraat 60, 9052 Gent, Belgium");
        assertEquals(50.9968091, gent.latitude, 0.01);
        assertEquals(3.6993929, gent.longitude, 0.01);
    }

    @Test
    @Ignore
    // see https://www.mashape.com/montanaflynn/geocode-location-lookup/support/5#
    public void testReverseForeign(){
        JSONObject json = Geocoder.reverseLookupAddress(45.262231, 19.851858);
        System.out.println(json.toJSONString());
        assertEquals("Saudi Arabia", json.get("country"));
    }

    @Test
    @Ignore
    // see https://www.mashape.com/montanaflynn/geocode-location-lookup/support/11#
    public void testReverse(){
        JSONObject json = Geocoder.reverseLookupAddress(51.5072, 0.1275);
        System.out.println(json.toJSONString());
        assertEquals("United Kingdom", json.get("country"));
    }
}