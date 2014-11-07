/**
 * Created by User: veselin
 * On Date: 24/12/13
 */

package com.ai.myplugin.sensor;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;


public class LatitudeLongitudeRawSensorTest{

    @Test
    @Ignore
    // see https://www.mashape.com/montanaflynn/geocode-location-lookup/support/5#
    public void testExecute() throws Exception {
        LatitudeLongitudeRawSensor locationSensor = new LatitudeLongitudeRawSensor();
        locationSensor.setProperty("longitude", 19.851858);
        locationSensor.setProperty("latitude", 45.262231);
        assertNotNull(locationSensor.execute(null).getRawData());
        assertEquals("Novi Sad", ((JSONObject)new JSONParser().parse(locationSensor.execute(null).getRawData())).get("city"));
    }
}
