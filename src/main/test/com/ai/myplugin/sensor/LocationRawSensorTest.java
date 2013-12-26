package com.ai.myplugin.sensor;

import junit.framework.TestCase;

/**
 * Created by User: veselin
 * On Date: 24/12/13
 */
public class LocationRawSensorTest extends TestCase{

    public void testExecuteLocation() throws Exception {
        LocationRawSensor locationRawSensor = new LocationRawSensor();
        locationRawSensor.setProperty("location", "Gent");
        assertNotNull(locationRawSensor.execute(null).getRawData());
    }
}
