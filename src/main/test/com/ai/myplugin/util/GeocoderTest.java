package com.ai.myplugin.util;

import org.junit.Test;

import static org.junit.Assert.*;

public class GeocoderTest {

    @Test
    public void testGetLongitudeLatitudeForAddress() throws Exception {
        Geocoder.LatLng gent = Geocoder.getLongitudeLatitudeForAddress("Krekelstraat 60, 9052 Gent, Belgium");
        assertEquals(new Geocoder.LatLng(50.9968091, 3.6862785), gent);
    }
}