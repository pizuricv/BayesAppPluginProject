/**
 * Created by User: veselin
 * On Date: 29/09/13
 */

package com.ai.myplugin.sensor;

import junit.framework.TestCase;
import org.json.simple.parser.ParseException;

import java.util.Map;


public class WeatherAbstractSensorTest extends TestCase{
    WeatherAbstractSensor weatherAbstractSensor;

    public void setup(){
        weatherAbstractSensor = new WeatherAbstractSensor() {
            @Override
            protected String getTag() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            protected String getSensorName() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }
        } ;
    }

    public void testNiceForecast() throws ParseException {
        Map map = weatherAbstractSensor.getForecast("Warm", "Clear", 90, 1000, 20, 1);
        assertEquals(0.8, Double.parseDouble(map.get("Good").toString()));

    }
    public void testBadForecast() throws ParseException {
        Map map  = weatherAbstractSensor.getForecast("Cold", "Clouds", 90, 1000, 80, 1);
        assertEquals(0.85, Double.parseDouble(map.get("Bad").toString()));

        map = weatherAbstractSensor.getForecast("Cold", "Clouds", 90, 1000, 20, 1);
        assertEquals(0.6, Double.parseDouble(map.get("Bad").toString()));

    }

    public void testStorm() throws ParseException {
        Map map= weatherAbstractSensor.getForecast("Cold", "Storm", 90, 1000, 80, 1);
        assertEquals(1, Double.parseDouble(map.get("Storm").toString()), 0.011);
    }
}
