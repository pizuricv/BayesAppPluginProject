package com.ai.myplugin.sensor;


import com.ai.api.DataType;
import com.ai.api.PluginHeader;
import com.ai.api.PropertyType;
import net.xeoh.plugins.base.annotations.PluginImplementation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@PluginImplementation
@PluginHeader(version = "1.0.1", author = "Veselin", category = "Weather", iconURL = "http://app.waylay.io/icons/weather_prediction.png")
public class WeatherWeekForecastSensor extends WeatherAbstractSensor{

    private static final Logger log = LoggerFactory.getLogger(WeatherWeekForecastSensor.class);

    private Boolean exactDay = false;
    private Integer days = 7;
    public static String EXACT_DAY = "exactDay";
    public static String DAYS = "Days";

    @Override
    protected String getTag() {
        return WEEK_FORECAST;
    }

    @Override
    public Map<String, PropertyType> getRequiredProperties() {
        Map<String, PropertyType> map = new HashMap<>();
        map.put(CITY, new PropertyType(DataType.STRING, true, false));
        map.put(DAYS, new PropertyType(DataType.INTEGER, true, false));
        map.put(EXACT_DAY, new PropertyType(DataType.BOOLEAN, true, false));
        return map;
    }

    @Override
    public void setProperty(String property, Object value) {
        if(property.equalsIgnoreCase(CITY)) {
            super.setProperty(property, value);
        } else if(property.equalsIgnoreCase(DAYS)) {
            days = Integer.parseInt(value.toString());
            if(days < 1 || days > 7)
                throw new IllegalArgumentException("Days must be in range [1-7] but was " + value);

        } else if(property.equalsIgnoreCase(EXACT_DAY)) {
            exactDay = Boolean.parseBoolean(value.toString());
        } else{
            throw new IllegalArgumentException("Property " + property + " not in the required settings");
        }
    }

    @Override
    public Object getProperty(String string) {
        if(string.equalsIgnoreCase(CITY))
            return super.getProperty(string);
        else if(string.equalsIgnoreCase(DAYS))
            return days;
        else if(string.equalsIgnoreCase(EXACT_DAY))
            return exactDay;
        else
            throw new RuntimeException("Property "+ string + " not in the required settings");
    }

    @Override
    protected String getSensorName() {
        return "WeatherWeekForecastSensor";
    }

//    public static void main(String[] args){
//        WeatherWeekForecastSensor weatherSensor = new WeatherWeekForecastSensor();
//        weatherSensor.setProperty("city", "Gent");
//        SensorResult testResult = weatherSensor.execute(null);
//        log.debug(testResult.getObserverStates());
//
//        weatherSensor.setProperty("city", "London");
//        testResult = weatherSensor.execute(null);
//        log.debug(testResult.getObserverStates());
//
//        weatherSensor.setProperty("city", "Sidney");
//        testResult = weatherSensor.execute(null);
//        log.debug(testResult.getObserverStates());
//
//        weatherSensor.setProperty("city", "Bangalore");
//        testResult = weatherSensor.execute(null);
//        log.debug(testResult.getObserverStates());
//
//        weatherSensor.setProperty("city", "Chennai");
//        testResult = weatherSensor.execute(null);
//        log.debug(testResult.getObserverStates());
//
//        weatherSensor.setProperty("city", "Moscow");
//        testResult = weatherSensor.execute(null);
//        log.debug(testResult.getObserverStates());
//
//        weatherSensor.setProperty("city", "Belgrade");
//        testResult = weatherSensor.execute(null);
//        log.debug(testResult.getObserverStates());
//
//        weatherSensor.setProperty("city", "Split");
//        testResult = weatherSensor.execute(null);
//        log.debug(testResult.getObserverStates());
//    }
}
