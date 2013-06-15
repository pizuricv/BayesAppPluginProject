
/**
 * User: pizuricv
 */
package com.ai.myplugin.sensor;

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
    static final String NAME = "Weather";
    static final String server = "http://api.openweathermap.org/";

    String [] weatherStates = {"Clouds", "Clear", "Rain",
            "Storm", "Snow", "Fog", "Mist" , "Drizzle",
            "Smoke", "Dust", "Tropical Storm", "Hot", "Cold" ,
            "Windy", "Hail"};

    @Override
    public String[] getRequiredProperties() {
        return new String[] {"City"};
    }

    @Override
    public void setProperty(String string, Object obj) {
        if(string.equalsIgnoreCase("city")) {
            city = (String) obj;
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
        final String stringToParse = stringBuffer.toString();
        System.out.println(stringToParse);

        //get weather ID
        int indexWeather = stringToParse.indexOf("weather\":[{\"id\"");
        String tempWeather = stringToParse.substring(indexWeather + 16 );
        int index1 = tempWeather.indexOf(",") == -1? Integer.MAX_VALUE : tempWeather.indexOf(",");
        int index2 = tempWeather.indexOf("},") == -1? Integer.MAX_VALUE : tempWeather.indexOf("},");
        final int weatherID = Integer.parseInt(tempWeather.substring(0, Math.min(index1, index2)));


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
                return mapWeather();
            }

            private String mapWeather() {
                //String [] weatherStates = {"Clouds", "Clear", "Rain", "Storm", "Snow", "Fog"};
                //http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
                if(weatherID < 300){
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
        };
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String[] getSupportedStates() {
        return weatherStates;
    }

    public static void main(String[] args){
        WeatherSensor weatherSensor = new WeatherSensor();
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
    }
}
