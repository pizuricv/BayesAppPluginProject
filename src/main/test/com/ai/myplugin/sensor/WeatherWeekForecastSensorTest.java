/**
 * Created by User: veselin
 * On Date: 23/10/13
 */

package com.ai.myplugin.sensor;

import com.ai.api.SensorResult;
import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.util.List;
import java.util.Map;


public class WeatherWeekForecastSensorTest extends TestCase{
    private static final Log log = LogFactory.getLog(WeatherWeekForecastSensorTest.class);

    public void testWeatherSensorCumulativeValue(){
        WeatherWeekForecastSensor weatherSensor = new WeatherWeekForecastSensor();
        weatherSensor.setProperty("city", "Gent");
        SensorResult SensorResult = weatherSensor.execute(null);
        List<Map<String, Number>> list = SensorResult.getObserverStates();
        double value = 0;
        for(String key: list.get(0).keySet())  {
            value += list.get(0).get(key).doubleValue();
            log.debug("State " + key + " = " + list.get(0).get(key).doubleValue());
        }
        assertEquals(1., value, 0.1);
    }

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
            log.debug("State " + key + " = " + list.get(0).get(key).doubleValue());
        }
        assertEquals(1., value);

        weatherSensor = new WeatherWeekForecastSensor();
        weatherSensor.setProperty("city", "Gent");
        weatherSensor.setProperty(WeatherWeekForecastSensor.DAYS, 5);
        weatherSensor.setProperty(WeatherWeekForecastSensor.EXACT_DAY, true);
        SensorResult = weatherSensor.execute(null);
        list = SensorResult.getObserverStates();
        value = 0;
        for(String key: list.get(0).keySet())  {
            value += list.get(0).get(key).doubleValue();
            log.debug("State " + key + " = " + list.get(0).get(key).doubleValue());
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
