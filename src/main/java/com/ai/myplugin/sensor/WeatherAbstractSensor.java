/**
 * User: pizuricv
 */

package com.ai.myplugin.sensor;

import com.ai.api.*;
import com.ai.myplugin.util.OpenWeatherParser;
import org.json.simple.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class WeatherAbstractSensor implements SensorPlugin {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    String city;
    public static final String TEMP = "temperature";
    public static final String HUMIDITY = "humidity";
    public static final String WEATHER = "weather";
    public static final String WIND_SPEED = "windSpeed";
    public static final String FORECAST = "forecast";
    public static final String WEEK_FORECAST = "weekForecast";
    public static final String CLOUD_COVERAGE = "cloudCoverage";
    public static final String PRESSURE = "pressure";
    static final String CITY = "city";

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
    String [] forecastStates = {"Good", "Bad", "NotSure"};
    private static final String NAME = "Weather";

    @Override
    public Map<String, PropertyType> getRequiredProperties() {
        Map<String, PropertyType> map = new HashMap<>();
        map.put(CITY, new PropertyType(DataType.STRING, true, false));
        return map;
    }

    @Override
    public Map<String, PropertyType> getRuntimeProperties() {
        return new HashMap<>();
    }

    @Override
    public void setProperty(String property, Object value) {
        if(property.equalsIgnoreCase(CITY)) {
            city = URLEncoder.encode((String) value);
        } else {
            throw new RuntimeException("Property "+ property + " not in the required settings");
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
    public SensorResult execute(SessionContext testSessionContext) {
        log.debug("execute "+ getName() + ", sensor type:" +this.getClass().getName());
        if(city == null){
            throw new RuntimeException("City not defined");
        }

        if(!WEEK_FORECAST.equals(getTag())) {

            final ConcurrentHashMap<String, Number> map = OpenWeatherParser.getWeatherResultCodes(city);

            final int finalHumidity = map.get(HUMIDITY).intValue();
            final int finalTemp = map.get(TEMP).intValue();
            final int finalWeatherID = map.get(WEATHER).intValue();
            final int finalPressure = map.get(PRESSURE).intValue();
            final double finalWindSpeed = map.get(WIND_SPEED).doubleValue();
            final int finalCloudCoverage = map.get(CLOUD_COVERAGE).intValue();
            return new SensorResult() {
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
                        JSONArray array = new JSONArray();
                        array.add(getForecast(mapTemperature(finalTemp), mapWeather(finalWeatherID),
                                finalHumidity, finalPressure, finalCloudCoverage, finalWindSpeed));
                        return array.toJSONString();
                    }else {
                        return mapHumidity(finalHumidity);
                    }
                }

                @Override
                public List<Map<String, Number>> getObserverStates() {
                    List list = null;
                    if(getTag().equals(FORECAST))  {
                        list = new ArrayList();
                        list.add(getForecast(mapTemperature(finalTemp), mapWeather(finalWeatherID),
                                finalHumidity, finalPressure, finalCloudCoverage, finalWindSpeed));
                    }
                    return list;
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
        }else {
            final List<Map<String, Number>> list = OpenWeatherParser.getWeatherResultForWeekCodes(city);
            final Boolean exactDay = (Boolean) getProperty(WeatherWeekForecastSensor.EXACT_DAY);
            final Integer days = (Integer) getProperty(WeatherWeekForecastSensor.DAYS);

            return new SensorResult() {
                @Override
                public boolean isSuccess() {
                    return true;
                }

                @Override
                public String getName() {
                    return "Weather week forecast";
                }

                //TODO see how to flood this information back, probably will require the update of the interface
                @Override
                public String getObserverState() {
                    return null;
                }

                @Override
                public List<Map<String, Number>> getObserverStates() {
                    ArrayList<Map<String, Number>> predictions = new ArrayList();
                    if(exactDay){
                        Map<String, Number> map = list.get(days);
                        int humidity = map.get(HUMIDITY).intValue();
                        int temperature = map.get(TEMP).intValue();
                        int weatherID = map.get(WEATHER).intValue();
                        int pressure = map.get(PRESSURE).intValue();
                        double windSpeed = map.get(WIND_SPEED).doubleValue();
                        int cloudCoverage = map.get(CLOUD_COVERAGE).intValue();
                        predictions.add(getForecast(mapTemperature(temperature), mapWeather(weatherID),
                                humidity, pressure, cloudCoverage, windSpeed));
                        return predictions;
                    }
                    List<Map<String, Number>> list2= list.subList(0, days);
                    for(Map mapIter : list2){
                        ConcurrentHashMap<String, Number> map = (ConcurrentHashMap<String, Number>) mapIter;
                        int humidity = map.get(HUMIDITY).intValue();
                        int temperature = map.get(TEMP).intValue();
                        int weatherID = map.get(WEATHER).intValue();
                        int pressure = map.get(PRESSURE).intValue();
                        double windSpeed = map.get(WIND_SPEED).doubleValue();
                        int cloudCoverage = map.get(CLOUD_COVERAGE).intValue();
                        predictions.add(getForecast(mapTemperature(temperature), mapWeather(weatherID),
                                humidity, pressure, cloudCoverage, windSpeed));
                    }
                    return getCumulativePredictions(predictions);
                }

                @Override
                public String getRawData() {
                    JSONArray jsonArray = new JSONArray();
                    for(Map map : list){
                        jsonArray.add(map);
                    }
                    return jsonArray.toJSONString();
                }
            } ;
        }
    }

    @Override
    public List<RawDataType> getRawDataTypes() {
        List<RawDataType> list = new ArrayList<>();
        boolean forecast = !WEEK_FORECAST.equals(getTag());//forecast creates a list with values per day
        list.add(new RawDataType("moving_average", "double", DataType.DOUBLE, forecast, CollectedType.INSTANT));
        list.add(new RawDataType("high", "double", DataType.DOUBLE, forecast, CollectedType.INSTANT));
        list.add(new RawDataType("price", "double", DataType.DOUBLE, forecast, CollectedType.INSTANT));
        list.add(new RawDataType("low", "double", DataType.DOUBLE, forecast, CollectedType.INSTANT));
        list.add(new RawDataType("percent", "double", DataType.DOUBLE, forecast, CollectedType.INSTANT));
        list.add(new RawDataType("volume", "double", DataType.DOUBLE, forecast, CollectedType.INSTANT));
        return list;
    }

    private static List<Map<String, Number>> getCumulativePredictions(ArrayList<Map<String , Number>> predictions) {
        Map<String, Number> resultMap = new ConcurrentHashMap<String, Number>();
        Double totalSum = 0.;
        for(Map<String, Number> map : predictions){
            for(String key: map.keySet()){
                Number res = resultMap.get(key) == null? 0:resultMap.get(key);
                Double value = res.doubleValue() + map.get(key).doubleValue();
                totalSum += map.get(key).doubleValue();
                resultMap.put(key, value);
            }
        }
        double coef = 1/totalSum;
        for(String key : resultMap.keySet())
            resultMap.put(key, Math.round(resultMap.get(key).doubleValue()* coef * 100)/100.);
        ArrayList l = new ArrayList<Map<String, Number>>();
        l.add(resultMap);
        return l;
    }

    private static double boolToDouble(boolean b) {
        return b ? 1.0 : 0.0;
    }
    public static Map<String, Number> getForecast(String temperature, String weather, int humidity, int pressure, int cloudCoverage, double windSpeed){
        Map<String, Number> map = new ConcurrentHashMap<String, Number>();
        String weatherTemp = (temperature + "_" + weather).toLowerCase();
        double goodId = boolToDouble(weatherTemp.contains("heat"))+ 2* boolToDouble(weatherTemp.contains("warm"))+
                boolToDouble(weatherTemp.contains("mild")) + 3* boolToDouble(weatherTemp.contains("clear"))+
                boolToDouble(weatherTemp.contains("hot"));
        double badID = boolToDouble(weatherTemp.contains("cold")) + 2* boolToDouble(weatherTemp.contains("freeze")) +
                boolToDouble(weatherTemp.contains("snow")) +  2* boolToDouble(weatherTemp.contains("rain")) +
                2* boolToDouble(weatherTemp.contains("cloud")) * cloudCoverage/100 + 0.5* boolToDouble(weatherTemp.contains("fog")) +
                0.5* boolToDouble(weatherTemp.contains("mist")) + 3* boolToDouble(weatherTemp.contains("extreme"));
        boolean stormID = weatherTemp.contains("storm") || weatherTemp.contains("tornado");

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
        return map;
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
        //log.debug("Map humidity "+ humidityId);
        if(humidityId < 70) {
            return "Low";
        } else if(humidityId < 90) {
            return "Normal";
        }
        return "High";
    }

    private String mapTemperature(int temperature) {
        //log.debug("Map temperature "+ temperature);
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
        } else if(FORECAST.equals(getTag()) || WEEK_FORECAST.equals(getTag())){
            return forecastStates;
        } else {
            return new String[]{};
        }
    }

    @Override
    public void setup(SessionContext testSessionContext) {
        log.debug("Setup : " + getName() + ", sensor : "+this.getClass().getName());
    }

    @Override
    public void shutdown(SessionContext testSessionContext) {
        log.debug("Shutdown : " + getName() + ", sensor : "+this.getClass().getName());
    }

}
