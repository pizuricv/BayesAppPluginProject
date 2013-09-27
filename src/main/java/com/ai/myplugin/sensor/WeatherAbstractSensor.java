
/**
 * User: pizuricv
 */
package com.ai.myplugin.sensor;

import com.ai.bayes.plugins.BNSensorPlugin;
import com.ai.bayes.scenario.TestResult;
import com.ai.myplugin.util.OpenWeatherParser;
import com.ai.util.resource.TestSessionContext;
import netscape.javascript.JSObject;
import org.json.simple.JSONObject;


import java.net.URLEncoder;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class WeatherAbstractSensor implements BNSensorPlugin {
    String city;
    public static final String TEMP = "temperature";
    public static final String HUMIDITY = "humidity";
    public static final String WEATHER = "weather";
    public static final String WIND_SPEED = "windSpeed";
    public static final String FORECAST = "forecast";
    public static final String CLOUD_COVERAGE = "cloudCoverage";
    public static final String PRESSURE = "pressure";
    static final String CITY = "city";
    static final String server = "http://api.openweathermap.org/";

    protected abstract String getTag();
    protected abstract String getSensorName();

    protected static String [] weatherStates = {"Clouds", "Clear", "Rain",
            "Storm", "Snow", "Fog", "Mist" , "Drizzle",
            "Smoke", "Dust", "Tropical Storm", "Hot", "Cold" ,
            "Windy", "Hail"};
    protected static String [] weatherForecastStates = {
            "Good", "Bad", "Storm", "NotSure"
    };
    String [] humidityStates = {"Low", "Normal", "High"};
    String [] tempStates = {"Freezing", "Cold", "Mild", "Warm", "Heat"};
    private static final String NAME = "Weather";

    @Override
    public String[] getRequiredProperties() {
        return new String[] {"City"};
    }

    @Override
    public void setProperty(String string, Object obj) {
        if(string.equalsIgnoreCase(CITY)) {
            city = URLEncoder.encode((String) obj);
        } else {
            throw new RuntimeException("Property "+ string + " not in the required settings");
        }
    }

    @Override
    public Object getProperty(String string) {
        return city;
    }

    @Override
    public String getDescription() {
        return "Weather information";
    }

    @Override
    public TestResult execute(TestSessionContext testSessionContext) {
        if(city == null){
            throw new RuntimeException("City not defined");
        }

        final ConcurrentHashMap<String, Number> map = OpenWeatherParser.getWeatherResultCodes(city);

        final int finalHumidity = map.get(HUMIDITY).intValue();
        final int finalTemp = map.get(TEMP).intValue();
        final int finalWeatherID = map.get(WEATHER).intValue();
        final int finalPressure = map.get(PRESSURE).intValue();
        final double finalWindSpeed = map.get(WIND_SPEED).doubleValue();
        final int finalCloudCoverage = map.get(CLOUD_COVERAGE).intValue();
        return new TestResult() {
            @Override
            public boolean isSuccess() {
                return true;
            }

            @Override
            public String getName() {
                return "Weather result";
            }

            @Override
            public String getObserverState() {
                if(getTag().equals(TEMP)){
                    return mapTemperature(finalTemp);
                } else if(getTag().equals(WEATHER)){
                    return mapWeather(finalWeatherID);
                } else if(getTag().equals(FORECAST)){
                    return getForecast(mapTemperature(finalTemp), mapWeather(finalWeatherID),
                            finalHumidity, finalPressure, finalCloudCoverage, finalWindSpeed);
                }else {
                    return mapHumidity(finalHumidity);
                }
            }

            @Override
            public String getRawData(){
                return "{" +
                        "\"temperature\" : " + finalTemp + "," +
                        "\"weather\" : " + "\""+mapWeather(finalWeatherID) + "\""+ "," +
                        "\"humidity\" : " + finalHumidity + "," +
                        "\"pressure\" : " + finalPressure + "," +
                        "\"cloudCoverage\" : " + finalCloudCoverage + "," +
                        "\"windSpeed\" : " + finalWindSpeed +
                        "}";
            }
        };
    }
    private String getForecast(String temperature, String weather, int humidity, int pressure, int cloudCoverage, double windSpeed){
        Map<String, Number> map = new ConcurrentHashMap<String, Number>();
        String weatherTemp = (temperature + "_" + weather).toLowerCase();
        double goodId = weatherTemp.split("heat").length-1 + 2*(weatherTemp.split("warm").length-1)+
                weatherTemp.split("mild").length-1 + 3*(weatherTemp.split("clear").length-1) +
                weatherTemp.split("hot").length-1;
        double badID = weatherTemp.split("cold").length-1 + 2*(weatherTemp.split("freeze").length-1) +
                weatherTemp.split("snow").length-1 + 2*(weatherTemp.split("snow").length-1) +
                2*(weatherTemp.split("rain").length-1) + 2*(weatherTemp.split("clouds").length-1) * cloudCoverage/100 +
                0.5*(weatherTemp.split("fog").length-1) + 0.5*(weatherTemp.split("mist").length-1) +
                3*(weatherTemp.split("extreme").length-1);
        boolean stormID = weatherTemp.split("storm").length > 1 || weatherTemp.split("tornado").length > 1;

        if(stormID){
            map.put("Storm", 0.99);
            map.put("NotSure", 0.01);
        } else {
            double coef = goodId - badID;
            if(coef > 3) {
                map.put("Good", 0.8);
                map.put("Bad", 0.1);
                map.put("NotSure", 0.1);
            } else if(coef > 2) {
                map.put("Good", 0.6);
                map.put("Bad", 0.3);
                map.put("NotSure", 0.1);
            } else if(coef > 1) {
                map.put("Good", 0.5);
                map.put("Bad", 0.2);
                map.put("NotSure", 0.3);
            } else if(coef > 0) {
                map.put("Good", 0.4);
                map.put("Bad", 0.3);
                map.put("NotSure", 0.3);
            } else if(coef > -1) {
                map.put("Good", 0.3);
                map.put("Bad", 0.5);
                map.put("NotSure", 0.2);
            } else if(coef > -2) {
                map.put("Good", 0.2);
                map.put("Bad", 0.6);
                map.put("NotSure", 0.2);
            } else {
                map.put("Good", 0.05);
                map.put("Bad", 0.85);
                map.put("NotSure", 0.1);
            }
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("states", map);
        return jsonObject.toJSONString();
    };

    private String mapWeather(int weatherID) {
        //http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
        if(weatherID == -1){
            return "No data";
        }else if(weatherID < 300){
            return "Storm";
        } else if(weatherID < 400){
            return "Drizzle";
        } else if(weatherID < 600){
            return "Rain";
        } else if(weatherID < 700){
            return "Snow";
        } else if(weatherID == 701){
            return "Mist";
        } else if(weatherID == 711){
            return "Smoke";
        } else if(weatherID == 721){
            return "Haze";
        } else if(weatherID == 731){
            return "Dust";
        } else if(weatherID == 741){
            return "Fog";
        } else if(weatherID == 800){
            return "Clear";
        } else if(weatherID < 900){
            return "Clouds";
        } else if(weatherID == 900){
            return "Tornado";
        } else if(weatherID == 901){
            return "Tropical Storm";
        } else if(weatherID == 902){
            return "Cold";
        } else if(weatherID == 903){
            return "Hot";
        } else if(weatherID == 904){
            return "Windy";
        }  else if(weatherID == 9035){
            return "Hail";
        }
        return "Extreme";

    }

    private String mapHumidity(int humidityId) {
        //    String [] humidityStates = {"Low", "Normal", "High"};
        System.out.println("Map humidity "+ humidityId);
        if(humidityId < 70) {
            return "Low";
        } else if(humidityId < 90) {
            return "Normal";
        }
        return "High";
    }

    private String mapTemperature(int temperature) {
        System.out.println("Map temperature "+ temperature);
        if(temperature < 0) {
            return "Freezing";
        }  else if(temperature < 8) {
            return "Cold";
        } else if(temperature < 15) {
            return "Mild";
        }  else if(temperature < 25) {
            return "Warm";
        }
        return "Heat";
    };

    @Override
    public String getName() {
        return getSensorName();
    }

    @Override
    public String[] getSupportedStates() {
        if(TEMP.equals(getTag())){
            return tempStates;
        } else if(WEATHER.equals(getTag())){
            return weatherStates;
        } else if(HUMIDITY.equals(getTag())){
            return humidityStates;
        } else {
            return new String[]{};
        }
    }

    public static void main(String[] args){
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
        TestResult testResult = weatherSensor.execute(null);
        System.out.println(testResult.getObserverState());


        weatherSensor.setProperty("city", "London");
        testResult = weatherSensor.execute(null);
        System.out.println(testResult.getObserverState());

        weatherSensor.setProperty("city", "Sidney");
        testResult = weatherSensor.execute(null);
        System.out.println(testResult.getObserverState());

        weatherSensor.setProperty("city", "Bangalore");
        testResult = weatherSensor.execute(null);
        System.out.println(testResult.getObserverState());

        weatherSensor.setProperty("city", "Chennai");
        testResult = weatherSensor.execute(null);
        System.out.println(testResult.getObserverState());

        weatherSensor.setProperty("city", "Moscow");
        testResult = weatherSensor.execute(null);
        System.out.println(testResult.getObserverState());

        weatherSensor.setProperty("city", "Belgrade");
        testResult = weatherSensor.execute(null);
        System.out.println(testResult.getObserverState());

        weatherSensor.setProperty("city", "Split");
        testResult = weatherSensor.execute(null);
        System.out.println(testResult.getObserverState());
        testResult.getRawData();


        System.out.println("@@@@");
        WeatherSensor weatherSensor1 = new WeatherSensor();
        weatherSensor1.setProperty("city", "ffffff");
        testResult = weatherSensor1.execute(null);
        System.out.println(testResult.getObserverState());
        System.out.println(testResult.getRawData());
    }
}
