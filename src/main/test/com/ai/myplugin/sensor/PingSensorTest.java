package com.ai.myplugin.sensor;

import com.ai.bayes.scenario.TestResult;
import junit.framework.TestCase;

/**
 * Created by User: veselin
 * On Date: 27/02/14
 */
public class PingSensorTest extends TestCase{
    public void testExecute() throws Exception {
        PingSensor pingSensor = new PingSensor();
        pingSensor.setProperty("address", "www.waylay.io");
        TestResult testResult = pingSensor.execute(null);
        System.out.println(testResult.getRawData());
        assertEquals("Alive", testResult.getObserverState());

        pingSensor.setProperty("ADDRESS", "www.waylay.io");
        testResult = pingSensor.execute(null);
        System.out.println(testResult.getRawData());
        assertEquals("Alive", testResult.getObserverState());

        pingSensor.setProperty("address", "www.waylaay.io");
        testResult = pingSensor.execute(null);
        System.out.println(testResult.getRawData());
        assertEquals("Not Alive", testResult.getObserverState());

    }
}
