/**
 * User: veselin
 */
package com.ai.myplugin.util;


import com.ai.bayes.scenario.TestResult;
import com.ai.myplugin.sensor.WeatherAbstractSensor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class OpenWeatherParser {
    private static final String server = "http://api.openweathermap.org/data/2.5/";

    private static String getServerDailyAddress(String city){
        return server + "weather?q=" + city + "&mode=json&units=metric&cnt=0";
    };

    private static String getServerForecastAddress(String city){
        return server + "forecast/daily?q=" + city + "&mode=json&units=metric&cnt=7";
    };

    private static JSONObject getWeatherResult(String city, String pathURL) {
        String stringToParse;
        try{
            stringToParse = Rest.httpGet(pathURL);
            System.out.println(stringToParse);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        JSONParser parser=new JSONParser();
        JSONObject obj  = null;
        try {
            obj = (JSONObject) parser.parse(stringToParse);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        if(obj.get("message")!=null && obj.get("message").toString().toLowerCase().contains("error"))
            throw new RuntimeException("ERROR getting weather info for: " + city + ", error:  "+obj.get("message").toString());
        return obj;
    }

    public static List<Map<String, Number>> getWeatherResultForWeekCodes(String city){
        String pathURL = getServerForecastAddress(city);

        ArrayList<Map<String, Number>> list = new ArrayList<Map<String, Number>>();
        JSONObject o = getWeatherResult(city, pathURL);
        JSONArray array = (JSONArray) o.get("list");
        for(Object object : array){
            JSONObject obj = (JSONObject) object;
            WeatherResultCodes weatherResultCodes = new WeatherResultCodes();

            weatherResultCodes.temp = Utils.getDouble( ((JSONObject)obj.get("temp")).get("day")).intValue();
            weatherResultCodes.humidity = Utils.getDouble(obj.get("humidity")).intValue();
            weatherResultCodes.pressure = Utils.getDouble(obj.get("pressure")).intValue();
            weatherResultCodes.windSpeed =  Utils.getDouble(obj.get("speed"));
            weatherResultCodes.cloudCoverage =  Utils.getDouble(obj.get("clouds")).intValue();

            JSONArray jsonArray2 = (JSONArray)obj.get("weather");
            for (Object o2 : jsonArray2.toArray()) {
                JSONObject jo2 = (JSONObject) o2;
                if(jo2.get("id") != null){
                    weatherResultCodes.weatherID =  Utils.getDouble(jo2.get("id")).intValue();
                }
            }
            list.add(weatherResultCodes.mapResults());
        }

        return list;
    }

    public static ConcurrentHashMap<String, Number> getWeatherResultCodes(String city){
        String pathURL = getServerDailyAddress(city);
        JSONObject obj = getWeatherResult(city, pathURL);

        WeatherResultCodes weatherResultCodes = new WeatherResultCodes();

        weatherResultCodes.temp = Utils.getDouble( ((JSONObject)obj.get("main")).get("temp")).intValue();
        weatherResultCodes.humidity = Utils.getDouble(((JSONObject)obj.get("main")).get("humidity")).intValue();
        weatherResultCodes.pressure = Utils.getDouble(((JSONObject)obj.get("main")).get("pressure")).intValue();
        weatherResultCodes.windSpeed =  Utils.getDouble(((JSONObject) obj.get("wind")).get("speed"));
        weatherResultCodes.cloudCoverage =  Utils.getDouble(((JSONObject)obj.get("clouds")).get("all")).intValue();
        JSONArray jsonArray1 = (JSONArray)obj.get("weather");
        for (Object o2 : jsonArray1.toArray()) {
            JSONObject jo2 = (JSONObject) o2;
            if(jo2.get("id") != null){
                weatherResultCodes.weatherID =  Utils.getDouble(jo2.get("id")).intValue();
            }
        }

        return weatherResultCodes.mapResults();

    }


    private static class WeatherResultCodes {
        int temp, humidity, weatherID, pressure, cloudCoverage;
        double windSpeed = -1;
        public ConcurrentHashMap<String, Number> mapResults(){
            ConcurrentHashMap<String, Number> map = new ConcurrentHashMap<String, Number>();
            map.put(WeatherAbstractSensor.TEMP, temp);
            map.put(WeatherAbstractSensor.WEATHER, weatherID);
            map.put(WeatherAbstractSensor.HUMIDITY, humidity);
            map.put(WeatherAbstractSensor.WIND_SPEED, windSpeed);
            map.put(WeatherAbstractSensor.CLOUD_COVERAGE, cloudCoverage);
            map.put(WeatherAbstractSensor.PRESSURE, pressure);
            return map;
        }
    }
}



/*
http://api.openweathermap.org/data/2.5/weather?q=London&mode=json
{
    "coord": {
        "lon": -0.12574,
        "lat": 51.50853
    },
    "sys": {
        "country": "GB",
        "sunrise": 1379742371,
        "sunset": 1379786427
    },
    "weather": [
        {
            "id": 803,
            "main": "Clouds",
            "description": "broken clouds",
            "icon": "04n"
        }
    ],
    "base": "gdps stations",
    "main": {
        "temp": 291.15,
        "pressure": 1025,
        "humidity": 82,
        "temp_min": 291.15,
        "temp_max": 291.15
    },
    "wind": {
        "speed": 3.6,
        "deg": 240
    },
    "clouds": {
        "all": 76
    },
    "dt": 1379789400,
    "id": 2643743,
    "name": "London",
    "cod": 200
}
 */

/*
http://api.openweathermap.org/data/2.5/forecast/daily?q=London&mode=json&units=metric&cnt=7


{
    "cod": "200",
    "message": 0.034,
    "city": {
        "id": 2643743,
        "name": "London",
        "coord": {
            "lon": -0.12574,
            "lat": 51.50853
        },
        "country": "GB",
        "population": 1000000
    },
    "cnt": 7,
    "list": [
        {
            "dt": 1379761200,
            "temp": {
                "day": 18,
                "min": 13.33,
                "max": 18,
                "night": 13.33,
                "eve": 18,
                "morn": 18
            },
            "pressure": 1029.43,
            "humidity": 77,
            "weather": [
                {
                    "id": 803,
                    "main": "Clouds",
                    "description": "broken clouds",
                    "icon": "04d"
                }
            ],
            "speed": 2.1,
            "deg": 227,
            "clouds": 76
        },
        {
            "dt": 1379847600,
            "temp": {
                "day": 21.29,
                "min": 9.6,
                "max": 22.77,
                "night": 11.19,
                "eve": 19.22,
                "morn": 9.6
            },
            "pressure": 1032.86,
            "humidity": 79,
            "weather": [
                {
                    "id": 800,
                    "main": "Clear",
                    "description": "sky is clear",
                    "icon": "01d"
                }
            ],
            "speed": 1.6,
            "deg": 248,
            "clouds": 0
        },
        {
            "dt": 1379934000,
            "temp": {
                "day": 22.2,
                "min": 9.31,
                "max": 22.8,
                "night": 14.44,
                "eve": 19.23,
                "morn": 9.31
            },
            "pressure": 1030.57,
            "humidity": 75,
            "weather": [
                {
                    "id": 800,
                    "main": "Clear",
                    "description": "sky is clear",
                    "icon": "01d"
                }
            ],
            "speed": 1.66,
            "deg": 134,
            "clouds": 0
        },
        {
            "dt": 1380020400,
            "temp": {
                "day": 22.05,
                "min": 12.29,
                "max": 22.9,
                "night": 12.29,
                "eve": 18.89,
                "morn": 18.04
            },
            "pressure": 1024.09,
            "humidity": 75,
            "weather": [
                {
                    "id": 800,
                    "main": "Clear",
                    "description": "sky is clear",
                    "icon": "01d"
                }
            ],
            "speed": 1.6,
            "deg": 161,
            "clouds": 0
        },
        {
            "dt": 1380106800,
            "temp": {
                "day": 20.83,
                "min": 10.87,
                "max": 21.92,
                "night": 13.86,
                "eve": 18.15,
                "morn": 10.87
            },
            "pressure": 1027.45,
            "humidity": 73,
            "weather": [
                {
                    "id": 801,
                    "main": "Clouds",
                    "description": "few clouds",
                    "icon": "02d"
                }
            ],
            "speed": 1.61,
            "deg": 331,
            "clouds": 12
        },
        {
            "dt": 1380193200,
            "temp": {
                "day": 18.85,
                "min": 11.44,
                "max": 18.85,
                "night": 11.44,
                "eve": 17.64,
                "morn": 13.08
            },
            "pressure": 1013.84,
            "humidity": 0,
            "weather": [
                {
                    "id": 500,
                    "main": "Rain",
                    "description": "light rain",
                    "icon": "10d"
                }
            ],
            "speed": 3.84,
            "deg": 238,
            "clouds": 58,
            "rain": 2.92
        },
        {
            "dt": 1380279600,
            "temp": {
                "day": 14.69,
                "min": 9.54,
                "max": 14.69,
                "night": 10.77,
                "eve": 14.69,
                "morn": 9.54
            },
            "pressure": 1027.81,
            "humidity": 0,
            "weather": [
                {
                    "id": 500,
                    "main": "Rain",
                    "description": "light rain",
                    "icon": "10d"
                }
            ],
            "speed": 6.13,
            "deg": 297,
            "clouds": 1,
            "rain": 1.35
        }
    ]
}
*/