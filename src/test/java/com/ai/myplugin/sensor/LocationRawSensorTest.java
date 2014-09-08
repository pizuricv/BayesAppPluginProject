/**
 * Created by User: veselin
 * On Date: 24/12/13
 */

package com.ai.myplugin.sensor;

import com.ai.api.SensorResult;
import junit.framework.TestCase;

public class LocationRawSensorTest extends TestCase{

    public void testExecuteLocation() throws Exception {
        LocationRawSensor locationRawSensor = new LocationRawSensor();
        locationRawSensor.setProperty("location", "Gent");

        SensorResult result = locationRawSensor.execute(null);

        assertTrue(result.isSuccess());
        assertNotNull(result.getRawData());
    }

    public void testExecuteLocationFail() throws Exception {
        LocationRawSensor locationRawSensor = new LocationRawSensor();
        locationRawSensor.setProperty("location", "");

        SensorResult result = locationRawSensor.execute(null);
        assertFalse(result.isSuccess());
    }
}
