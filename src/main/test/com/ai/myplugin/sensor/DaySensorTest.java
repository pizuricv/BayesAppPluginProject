package com.ai.myplugin.sensor;

import com.ai.bayes.scenario.TestResult;
import junit.framework.TestCase;

import java.util.Date;

/**
 * Created by User: veselin
 * On Date: 29/09/13
 */
public class DaySensorTest extends TestCase {

    public void testDay() {
        DaySensor daySensor = new DaySensor();
        TestResult result = daySensor.execute(null);
        assertNotNull(result.getObserverState());
    }
}
