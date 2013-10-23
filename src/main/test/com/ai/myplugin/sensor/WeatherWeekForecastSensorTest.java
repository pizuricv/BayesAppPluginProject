package com.ai.myplugin.sensor;

import com.ai.bayes.scenario.TestResult;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by User: veselin
 * On Date: 23/10/13
 */
public class WeatherWeekForecastSensorTest extends TestCase{

    public void testWeatherSensorCumulativeValue(){
        WeatherWeekForecastSensor weatherSensor = new WeatherWeekForecastSensor();
        weatherSensor.setProperty("city", "Gent");
        TestResult testResult = weatherSensor.execute(null);
        List<Map<String, Number>> list = testResult.getObserverStates();
        double value = 0;
        for(String key: list.get(0).keySet())  {
            value += list.get(0).get(key).doubleValue();
            System.out.println("State " + key + " = "+list.get(0).get(key).doubleValue());
        }
        assertEquals(1., value);
    }

    public void testWeatherSensorCumulativeValueForDays(){
        WeatherWeekForecastSensor weatherSensor = new WeatherWeekForecastSensor();
        weatherSensor.setProperty("city", "Gent");
        weatherSensor.setProperty(WeatherWeekForecastSensor.DAYS, 5);
        weatherSensor.setProperty(WeatherWeekForecastSensor.EXACT_DAY, false);
        TestResult testResult = weatherSensor.execute(null);
        List<Map<String, Number>> list = testResult.getObserverStates();
        double value = 0;
        for(String key: list.get(0).keySet())  {
            value += list.get(0).get(key).doubleValue();
            System.out.println("State " + key + " = "+list.get(0).get(key).doubleValue());
        }
        assertEquals(1., value);

        weatherSensor = new WeatherWeekForecastSensor();
        weatherSensor.setProperty("city", "Gent");
        weatherSensor.setProperty(WeatherWeekForecastSensor.DAYS, 5);
        weatherSensor.setProperty(WeatherWeekForecastSensor.EXACT_DAY, true);
        testResult = weatherSensor.execute(null);
        list = testResult.getObserverStates();
        value = 0;
        for(String key: list.get(0).keySet())  {
            value += list.get(0).get(key).doubleValue();
            System.out.println("State " + key + " = "+list.get(0).get(key).doubleValue());
        }
        assertEquals(1., value);
    }

    public void testProperties(){
        WeatherWeekForecastSensor weatherWeekForecastSensor = new WeatherWeekForecastSensor();
        try{
            weatherWeekForecastSensor.setProperty("days", 8);
            fail();
        } catch (Exception e){

        }
        try{
            weatherWeekForecastSensor.setProperty("days", -1);
            fail();
        } catch (Exception e){

        }
        weatherWeekForecastSensor.setProperty("exactDay", false);
        weatherWeekForecastSensor.setProperty("exactDay", "true");
        assertEquals(weatherWeekForecastSensor.getProperty("exactDay"), true);
    }
}
