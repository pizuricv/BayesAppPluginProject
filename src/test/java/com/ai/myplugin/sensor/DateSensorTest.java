package com.ai.myplugin.sensor;

import org.junit.Test;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import static org.junit.Assert.*;

public class DateSensorTest {

    @Test
    public void testActionNoMatch(){
        DateSensor sensor = new DateSensor();
        sensor.setProperty(DateSensor.DATE_FORMAT, "2013-09-10T00:00:00.000Z");
        sensor.setProperty(DateSensor.TIME_ZONE, "UTC");

        assertTrue(sensor.getSupportedStates().contains("true"));
        assertTrue(sensor.getSupportedStates().contains("false"));

        String result = sensor.execute(null).getObserverState();
        assertEquals("false", result);
    }

    @Test
    public void testActionMatch(){
        DateSensor sensor = new DateSensor();
        sensor.setProperty(DateSensor.DATE_FORMAT, LocalDateTime.now().atZone(ZoneId.of("UTC")).toString());
        sensor.setProperty(DateSensor.TIME_ZONE, "UTC");
        String result = sensor.execute(null).getObserverState();
        assertEquals("true", result);
    }

}