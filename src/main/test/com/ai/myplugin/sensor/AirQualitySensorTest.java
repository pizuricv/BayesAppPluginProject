package com.ai.myplugin.sensor;

import com.ai.bayes.scenario.TestResult;
import junit.framework.TestCase;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Created by User: veselin
 * On Date: 24/10/13
 */
public class AirQualitySensorTest extends TestCase{
    public void testAirQualitySensor() throws ParseException {
        AirQualitySensor airQualitySensor = new AirQualitySensor();
        airQualitySensor.setProperty(AirQualitySensor.LOCATION, "Gent");
        TestResult testResult = airQualitySensor.execute(null);
        System.out.println(testResult.getObserverState());
        System.out.println(testResult.getRawData());
        JSONParser parser = new JSONParser();
        long value = (Long)(((JSONObject)parser.parse(testResult.getRawData())).get("value"));
        assertTrue(value < 11);
    }
}
