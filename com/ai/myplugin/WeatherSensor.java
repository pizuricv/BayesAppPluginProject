
/**
 * User: pizuricv
 */
package com.ai.myplugin;

import com.ai.bayes.plugins.BNSensorPlugin;
import com.ai.bayes.scenario.TestResult;
import com.ai.util.resource.TestSessionContext;
import net.xeoh.plugins.base.annotations.PluginImplementation;

import java.io.*;
import java.net.*;
import java.util.Properties;

@PluginImplementation
public class WeatherSensor implements BNSensorPlugin{
    //TODO use JSON parsing later, need a tiny library for this
    String city;
    static final String TEMP = "temperature";
    static final String HUMIDITY = "humidity";
    //static final String WEATHER = "weather";
    static final String OPTION = "option";
    static final String CITY = "city";
    static final String server = "http://api.openweathermap.org/";

    //default option
    Properties property = new Properties();

    //String [] weatherStates = {"Cloudy", "Clear", "Raining", "Storm", "Snow"};

    @Override
    public String[] getRequiredProperties() {
        return new String[] {"City", "Option"};
    }

    @Override
    public void setProperty(String string, Object obj) {
        if(string.equalsIgnoreCase(OPTION)) {
            if(!obj.toString().equalsIgnoreCase(TEMP) && !obj.toString().equalsIgnoreCase(HUMIDITY)){
                throw new RuntimeException("Property "+ obj + " not in the required settings");
            }
            property.put(OPTION, obj);
        } else if(string.equalsIgnoreCase(CITY)) {
            city = (String) obj;
        } else {
            throw new RuntimeException("Property "+ string + " not in the required settings");
        }
    }

    @Override
    public Object getProperty(String string) {
        return property.get(string);
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

        URL url;
        boolean testSuccess = true;

        try {
            url = new URL(server+ "data/2.5/find?q="+ city+ "&mode=json&units=metric&cnt=0");
        } catch (MalformedURLException e) {
            System.err.println(e.getLocalizedMessage());
            throw new RuntimeException(e);
        }
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
            testSuccess = false;
        }
        assert conn != null;
        try {
            conn.setRequestMethod("GET");
        } catch (ProtocolException e) {
            e.printStackTrace();
            testSuccess = false;
        }

        BufferedReader rd = null;
        try {
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
            testSuccess = false;
        }
        String inputLine;
        StringBuffer stringBuffer = new StringBuffer();

        assert rd != null;
        try {
            while ((inputLine = rd.readLine()) != null){
                stringBuffer.append(inputLine);
            }
        } catch (IOException e) {
            e.printStackTrace();
            testSuccess = false;
        }
        conn.disconnect();
        try {
            rd.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

/*        JSONParser parser=new JSONParser();

        Object obj = parser.parse(stringBuffer.toString());*/
        String stringToParse = stringBuffer.toString();
        final int temp = (int)Math.round(Double.parseDouble(stringToParse.substring(stringToParse.indexOf("temp") + 6).substring(0, 3)));
        final int humidity = Integer.parseInt(stringToParse.substring(stringToParse.indexOf("humidity") + 10).substring(0, 2));

        final boolean finalTestSuccess = testSuccess;
        return new TestResult() {
            @Override
            public boolean isSuccess() {
                return finalTestSuccess;
            }

            @Override
            public String getName() {
                return "Weather result";
            }

            @Override
            public String getObserverState() {
                if(property.get(OPTION).equals(TEMP)){
                    return mapTemperature();
                } else {
                    return mapHumidity();
                }
            }

            private String mapHumidity() {
                //    String [] humidityStates = {"Low", "Normal", "High"};
                System.out.println("Map humidity "+humidity);
                if(humidity < 70) {
                    return "Low";
                } else if(humidity < 90) {
                    return "Normal";
                }
                return "High";
            }

            private String mapTemperature() {

                //    String [] tempStates = {"Freezing", "Cold", "Mild", "Warm", "Heat"};
                System.out.println("Map temperature "+temp);
                if(temp < 0) {
                    return "Freezing";
                }  else if(temp < 8) {
                    return "Cold";
                } else if(temp < 15) {
                    return "Mild";
                }  else if(temp < 25) {
                    return "Warm";
                }
                return "Heat";
            };
        };
    }

    @Override
    public String getName() {
        return "Weather result";
    }

    @Override
    public String[] getSupportedStates() {
        return new String[] {};
    }

    public static void main(String[] args){
        WeatherSensor weatherSensor = new WeatherSensor();
        weatherSensor.setProperty("option", WeatherSensor.HUMIDITY);
        weatherSensor.setProperty("city", "Gent");
        TestResult testResult = weatherSensor.execute(null);
        System.out.println(testResult.getObserverState());
        weatherSensor.setProperty("option", WeatherSensor.TEMP);
        weatherSensor.setProperty("city", "London");
        testResult = weatherSensor.execute(null);
        System.out.println(testResult.getObserverState());

    }
}
