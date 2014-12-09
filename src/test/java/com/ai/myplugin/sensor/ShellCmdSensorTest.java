package com.ai.myplugin.sensor;

import com.ai.api.SensorResult;
import com.ai.api.SessionContext;
import com.ai.myplugin.TestSessionContext;
import org.junit.Test;

import static org.junit.Assert.*;

public class ShellCmdSensorTest {

    @Test
    public void testExecute() throws Exception {
        ShellCmdSensor sensor = new ShellCmdSensor();
        sensor.setProperty(ShellCmdSensor.PROPERTY_COMMAND, "echo result=10");
        SessionContext context = new TestSessionContext();
        SensorResult result = sensor.execute(context);
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("result=10", result.getRawData());
        assertEquals("level_0", result.getObserverState());
    }

    @Test
    public void testExecuteWith() throws Exception {
        ShellCmdSensor sensor = new ShellCmdSensor();
        sensor.setProperty(ShellCmdSensor.PROPERTY_COMMAND, "echo result=6");
        sensor.setProperty(ShellCmdSensor.PROPERTY_THRESHOLD, "2,3,5,8,13");
        SessionContext context = new TestSessionContext();
        SensorResult result = sensor.execute(context);
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("result=6", result.getRawData());
        assertEquals("level_3", result.getObserverState());
    }

    @Test
    public void testSymmetry() throws Exception {
        ShellCmdSensor sensor = new ShellCmdSensor();
        String set = "2,3,5,8,13";
        sensor.setProperty(ShellCmdSensor.PROPERTY_THRESHOLD, "2,3,5,8,13");
        String get = (String) sensor.getProperty(ShellCmdSensor.PROPERTY_THRESHOLD);
        assertEquals(set, get);
    }
}