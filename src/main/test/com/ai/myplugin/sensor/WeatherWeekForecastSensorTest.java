/**
 * Created by User: veselin
 * On Date: 23/10/13
 */

package com.ai.myplugin.sensor;

import com.ai.api.SensorResult;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;


public class WeatherWeekForecastSensorTest{

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testWeatherSensorCumulativeValue(){
        WeatherWeekForecastSensor weatherSensor = new WeatherWeekForecastSensor();
        weatherSensor.setProperty("city", "Gent");
        SensorResult SensorResult = weatherSensor.execute(null);
        List<Map<String, Number>> list = SensorResult.getObserverStates();
        double value = 0;
        for(String key: list.get(0).keySet())  {
            value += list.get(0).get(key).doubleValue();
            System.out.println("State " + key + " = " + list.get(0).get(key).doubleValue());
        }
        assertEquals(1., value, 0.1);
    }

    @Test
    public void testWeatherSensorCumulativeValueForDays(){
        WeatherWeekForecastSensor weatherSensor = new WeatherWeekForecastSensor();
        weatherSensor.setProperty("city", "Gent");
        weatherSensor.setProperty(WeatherWeekForecastSensor.DAYS, 5);
        weatherSensor.setProperty(WeatherWeekForecastSensor.EXACT_DAY, false);
        SensorResult SensorResult = weatherSensor.execute(null);
        List<Map<String, Number>> list = SensorResult.getObserverStates();
        double value = 0;
        for(String key: list.get(0).keySet())  {
            value += list.get(0).get(key).doubleValue();
            System.out.println("State " + key + " = " + list.get(0).get(key).doubleValue());
        }
        assertEquals(1., value, 0.1);

        weatherSensor = new WeatherWeekForecastSensor();
        weatherSensor.setProperty("city", "Gent");
        weatherSensor.setProperty(WeatherWeekForecastSensor.DAYS, 5);
        weatherSensor.setProperty(WeatherWeekForecastSensor.EXACT_DAY, true);
        SensorResult = weatherSensor.execute(null);
        list = SensorResult.getObserverStates();
        value = 0;
        for(String key: list.get(0).keySet())  {
            value += list.get(0).get(key).doubleValue();
            System.out.println("State " + key + " = " + list.get(0).get(key).doubleValue());
        }
        assertEquals(1., value, 0.1);
    }

    @Test
    public void testProperties() {
        WeatherWeekForecastSensor weatherWeekForecastSensor = new WeatherWeekForecastSensor();

        weatherWeekForecastSensor.setProperty("exactDay", false);
        weatherWeekForecastSensor.setProperty("exactDay", "true");
        assertEquals(weatherWeekForecastSensor.getProperty("exactDay"), true);
    }


    @Test
    public void testPropertiesFail(){
        WeatherWeekForecastSensor weatherWeekForecastSensor = new WeatherWeekForecastSensor();

        expectedException.expect(IllegalArgumentException.class);

        weatherWeekForecastSensor.setProperty("days", 8);
    }

    @Test
    public void testPropertiesFail2(){
        WeatherWeekForecastSensor weatherWeekForecastSensor = new WeatherWeekForecastSensor();

        expectedException.expect(IllegalArgumentException.class);

        weatherWeekForecastSensor.setProperty("days", -1);
    }


}
