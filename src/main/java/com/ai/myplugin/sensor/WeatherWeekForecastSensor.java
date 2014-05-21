package com.ai.myplugin.sensor;


import com.ai.api.DataType;
import com.ai.api.PropertyType;
import com.ai.api.SensorResult;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@PluginImplementation

public class WeatherWeekForecastSensor extends WeatherAbstractSensor{
    private static final Log log = LogFactory.getLog(WeatherWeekForecastSensor.class);
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
    public void setProperty(String string, Object obj) {
        if(string.equalsIgnoreCase(CITY)) {
            super.setProperty(string, obj);
        } else if(string.equalsIgnoreCase(DAYS)) {
            days = Integer.parseInt(obj.toString());
            if(days < 1 || days > 7)
                throw new RuntimeException("Days "+ obj + " must be between [1-7]");

        } else if(string.equalsIgnoreCase(EXACT_DAY)) {
            exactDay = Boolean.parseBoolean(obj.toString());
        } else{
            throw new RuntimeException("Property "+ string + " not in the required settings");
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

    public static void main(String[] args){
        WeatherWeekForecastSensor weatherSensor = new WeatherWeekForecastSensor();
        weatherSensor.setProperty("city", "Gent");
        SensorResult testResult = weatherSensor.execute(null);
        log.debug(testResult.getObserverStates());

        weatherSensor.setProperty("city", "London");
        testResult = weatherSensor.execute(null);
        log.debug(testResult.getObserverStates());

        weatherSensor.setProperty("city", "Sidney");
        testResult = weatherSensor.execute(null);
        log.debug(testResult.getObserverStates());

        weatherSensor.setProperty("city", "Bangalore");
        testResult = weatherSensor.execute(null);
        log.debug(testResult.getObserverStates());

        weatherSensor.setProperty("city", "Chennai");
        testResult = weatherSensor.execute(null);
        log.debug(testResult.getObserverStates());

        weatherSensor.setProperty("city", "Moscow");
        testResult = weatherSensor.execute(null);
        log.debug(testResult.getObserverStates());

        weatherSensor.setProperty("city", "Belgrade");
        testResult = weatherSensor.execute(null);
        log.debug(testResult.getObserverStates());

        weatherSensor.setProperty("city", "Split");
        testResult = weatherSensor.execute(null);
        log.debug(testResult.getObserverStates());
    }
}
