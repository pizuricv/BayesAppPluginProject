package com.ai.myplugin.sensor;

import junit.framework.TestCase;

/**
 * Created by User: veselin
 * On Date: 24/12/13
 */
public class LocationSensorTest extends TestCase{

    public void testExecuteLocation() throws Exception {
        LocationSensor locationSensor = new LocationSensor();
        locationSensor.setProperty("city", "Gent");
        assertNotNull(locationSensor.execute(null).getRawData());
    }
}
