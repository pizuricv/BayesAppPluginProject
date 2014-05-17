package com.ai.myplugin.sensor;

import com.ai.api.SensorResult;
import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Created by User: veselin
 * On Date: 24/10/13
 */
public class AirQualitySensorTest extends TestCase{
    private static final Log log = LogFactory.getLog(AirQualitySensorTest.class);
    public void testAirQualitySensor() throws ParseException {
        AirQualitySensor airQualitySensor = new AirQualitySensor();
        airQualitySensor.setProperty(AirQualitySensor.LOCATION, "Gent");
        SensorResult testResult = airQualitySensor.execute(null);
        log.debug(testResult.getObserverState());
        log.debug(testResult.getRawData());
        JSONParser parser = new JSONParser();
        long value = (Long)(((JSONObject)parser.parse(testResult.getRawData())).get("value"));
        assertTrue(value < 11);
    }
}
