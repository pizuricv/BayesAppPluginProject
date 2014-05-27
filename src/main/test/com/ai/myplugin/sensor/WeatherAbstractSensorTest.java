/**
 * Created by User: veselin
 * On Date: 29/09/13
 */

package com.ai.myplugin.sensor;

import com.ai.api.SensorResult;
import org.json.simple.parser.ParseException;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Map;


public class WeatherAbstractSensorTest{
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

    @Test
    public void testNiceForecast() throws ParseException {
        Map map = weatherAbstractSensor.getForecast("Warm", "Clear", 90, 1000, 20, 1);
        assertEquals(0.8, Double.parseDouble(map.get("Good").toString()), 0.01);

    }

    @Test
    public void testBadForecast() throws ParseException {
        Map map  = weatherAbstractSensor.getForecast("Cold", "Clouds", 90, 1000, 80, 1);
        assertEquals(0.85, Double.parseDouble(map.get("Bad").toString()), 0.01);

        map = weatherAbstractSensor.getForecast("Cold", "Clouds", 90, 1000, 20, 1);
        assertEquals(0.6, Double.parseDouble(map.get("Bad").toString()), 0.01);

    }

    @Test
    public void testStorm() throws ParseException {
        Map map= weatherAbstractSensor.getForecast("Cold", "Storm", 90, 1000, 80, 1);
        assertEquals(1, Double.parseDouble(map.get("Storm").toString()), 0.02);
    }

    @Test
    public void testExecute(){
        WeatherAbstractSensor weatherSensor = new WeatherAbstractSensor() {
            @Override
            protected String getTag() {
                return WEATHER;
            }

            @Override
            protected String getSensorName() {
                return "";
            }
        };
        weatherSensor.setProperty("city", "Gent");
        SensorResult testResult = weatherSensor.execute(null);
        System.out.println(testResult.getObserverState());
//
//
//        weatherSensor.setProperty("city", "London");
//        testResult = weatherSensor.execute(null);
//        System.out.println(testResult.getObserverState());
//
//        weatherSensor.setProperty("city", "Sidney");
//        testResult = weatherSensor.execute(null);
//        System.out.println(testResult.getObserverState());
//
//        weatherSensor.setProperty("city", "Bangalore");
//        testResult = weatherSensor.execute(null);
//        System.out.println(testResult.getObserverState());
//
//        weatherSensor.setProperty("city", "Chennai");
//        testResult = weatherSensor.execute(null);
//        System.out.println(testResult.getObserverState());
//
//        weatherSensor.setProperty("city", "Moscow");
//        testResult = weatherSensor.execute(null);
//        System.out.println(testResult.getObserverState());
//
//        weatherSensor.setProperty("city", "Belgrade");
//        testResult = weatherSensor.execute(null);
//        System.out.println(testResult.getObserverState());

        weatherSensor.setProperty("city", "Split");
        testResult = weatherSensor.execute(null);
        System.out.println(testResult.getObserverState());
        testResult.getRawData();

    }

    @Test(expected = RuntimeException.class)
    public void testExecuteFail(){
        WeatherSensor weatherSensor1 = new WeatherSensor();
        weatherSensor1.setProperty("city", "ffffff");
        SensorResult testResult = weatherSensor1.execute(null);
    }
}
