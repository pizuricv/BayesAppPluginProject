package com.ai.myplugin.sensor;

import com.ai.bayes.scenario.TestResult;
import junit.framework.TestCase;

/**
 * Created by User: veselin
 * On Date: 22/10/13
 */
public class WaterLevelSensorTest extends TestCase {

    public void testWaterLevelSensorAlarm(){
        WaterLevelSensor waterLevelSensor = new WaterLevelSensor();
        waterLevelSensor.setProperty(WaterLevelSensor.LOCATION, "Neerslag Vinderhoute");
        waterLevelSensor.setProperty(WaterLevelSensor.DAILY_THRESHOLD, 1500);
        waterLevelSensor.setProperty(WaterLevelSensor.TOTAL_THRESHOLD, -100);
        TestResult testResult = waterLevelSensor.execute(null);
        assertTrue(testResult.getObserverState().equals("Alarm"));
        System.out.println(testResult.getObserverState());
        System.out.println(testResult.getRawData());
    }

    public void testWaterLevelSensorNoAlarm(){
        WaterLevelSensor waterLevelSensor = new WaterLevelSensor();
        waterLevelSensor.setProperty(WaterLevelSensor.LOCATION, "Neerslag Vinderhoute");
        waterLevelSensor.setProperty(WaterLevelSensor.DAILY_THRESHOLD, 1500);
        waterLevelSensor.setProperty(WaterLevelSensor.TOTAL_THRESHOLD, 1500);
        TestResult testResult = waterLevelSensor.execute(null);
        assertTrue(testResult.getObserverState().equals("No Alarm"));
        System.out.println(testResult.getObserverState());
        System.out.println(testResult.getRawData());
    }
}
