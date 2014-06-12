package com.ai.myplugin.sensor;

import com.ai.api.SensorResult;
import com.ai.api.SessionContext;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

public class XivelySensorTest {

    @Test
    @Ignore("integration test")
    public void testExecute() throws Exception {
        XivelySensor sensor = new XivelySensor();

        SessionContext context = new SessionContext(1);
        SensorResult result = sensor.execute(context);
        assertNotNull(result);
        System.out.println(result.getRawData());
    }
}