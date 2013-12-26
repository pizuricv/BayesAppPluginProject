package com.ai.myplugin.sensor;

import junit.framework.TestCase;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * Created by User: veselin
 * On Date: 24/12/13
 */
public class LatitudeLongitudeRawSensorTest extends TestCase{
    public void testExecute() throws Exception {
        LatitudeLongitudeRawSensor locationSensor = new LatitudeLongitudeRawSensor();
        locationSensor.setProperty("longitude", 19.851858);
        locationSensor.setProperty("latitude", 45.262231);
        assertNotNull(locationSensor.execute(null).getRawData());
        assertEquals("Novi Sad", ((JSONObject)new JSONParser().parse(locationSensor.execute(null).getRawData())).get("city"));
    }
}
