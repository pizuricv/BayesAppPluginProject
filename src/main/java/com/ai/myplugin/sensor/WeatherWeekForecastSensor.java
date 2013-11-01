package com.ai.myplugin.sensor;


import net.xeoh.plugins.base.annotations.PluginImplementation;
import com.ai.bayes.scenario.TestResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Arrays;

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
    public String[] getRequiredProperties() {
        String[] result = Arrays.copyOf(super.getRequiredProperties(), super.getRequiredProperties().length + 2);
        System.arraycopy(new String[]{DAYS, EXACT_DAY}, 0, result, super.getRequiredProperties().length, 2);
        return result;
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
        if(string.equalsIgnoreCase("city"))
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
        TestResult testResult = weatherSensor.execute(null);
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
