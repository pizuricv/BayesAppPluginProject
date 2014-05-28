/**
 * Created by User: veselin
 * On Date: 29/09/13
 */

package com.ai.myplugin.sensor;

import com.ai.api.SensorResult;
import junit.framework.TestCase;

public class DaySensorTest extends TestCase {

    public void testDay() {
        DaySensor daySensor = new DaySensor();
        SensorResult result = daySensor.execute(null);
        assertNotNull(result.getObserverState());
    }
}
