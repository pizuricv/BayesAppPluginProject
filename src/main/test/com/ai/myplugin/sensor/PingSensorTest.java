/**
 * Created by User: veselin
 * On Date: 27/02/14
 */
package com.ai.myplugin.sensor;

import com.ai.api.SensorResult;
import com.ai.api.SessionContext;
import org.junit.Test;
import static org.junit.Assert.*;


public class PingSensorTest{

    @Test
    public void testExecute() throws Exception {
        PingSensor pingSensor = new PingSensor();
        pingSensor.setProperty("address", "www.waylay.io");
        SensorResult testResult = pingSensor.execute(new SessionContext(1));
        System.out.println(testResult.getRawData());
        assertEquals("Alive", testResult.getObserverState());
    }

    @Test
    public void testExecuteUppercaseProperty() throws Exception {
        PingSensor pingSensor = new PingSensor();
        pingSensor.setProperty("ADDRESS", "www.waylay.io");
        SensorResult testResult = pingSensor.execute(new SessionContext(1));
        System.out.println(testResult.getRawData());
        assertEquals("Alive", testResult.getObserverState());
    }

    @Test
    public void testDown() throws Exception {
        PingSensor pingSensor = new PingSensor();
        pingSensor.setProperty("address", "www.waylaay.io");
        SensorResult testResult = pingSensor.execute(new SessionContext(1));
        System.out.println(testResult.getRawData());
        assertEquals("Not Alive", testResult.getObserverState());
    }
}
