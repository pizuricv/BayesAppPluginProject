package com.ai.myplugin.sensor;

import com.ai.bayes.scenario.TestResult;
import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Created by User: veselin
 * On Date: 22/10/13
 */
public class WaterLevelSensorTest extends TestCase {
    private static final Log log = LogFactory.getLog(WaterLevelSensorTest.class);

    public void testWaterLevelSensorAlarm(){
        WaterLevelSensor waterLevelSensor = new WaterLevelSensor();
        waterLevelSensor.setProperty(WaterLevelSensor.LOCATION, "PDM-438-R");
        waterLevelSensor.setProperty(WaterLevelSensor.DAILY_THRESHOLD, 1500);
        waterLevelSensor.setProperty(WaterLevelSensor.TOTAL_THRESHOLD, -100);
        TestResult testResult = waterLevelSensor.execute(null);
        assertTrue(testResult.getObserverState().equals("Alarm"));
        log.debug(testResult.getObserverState());
        log.debug(testResult.getRawData());
    }

    public void testWaterLevelSensorNoAlarm(){
        WaterLevelSensor waterLevelSensor = new WaterLevelSensor();
        waterLevelSensor.setProperty(WaterLevelSensor.LOCATION, "PDM-438-R");
        waterLevelSensor.setProperty(WaterLevelSensor.DAILY_THRESHOLD, 1500);
        waterLevelSensor.setProperty(WaterLevelSensor.TOTAL_THRESHOLD, 1500);
        TestResult testResult = waterLevelSensor.execute(null);
        assertTrue(testResult.getObserverState().equals("No Alarm"));
        log.debug(testResult.getObserverState());
        log.debug(testResult.getRawData());
    }
}
