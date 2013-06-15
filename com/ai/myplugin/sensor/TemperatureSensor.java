
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
public class TemperatureSensor implements BNSensorPlugin{
    //TODO use JSON parsing later, need a tiny library for this
    String city;
    static final String TEMP = "temperature";
    static final String CITY = "city";
    static final String server = "http://api.openweathermap.org/";

    //default option
    Properties property = new Properties();

    String [] tempStates = {"Freezing", "Cold", "Mild", "Warm", "Hot", "Heat"};
    private static final String NAME = "Temperature";

    @Override
    public String[] getRequiredProperties() {
        return new String[] {"City"};
    }

    @Override
    public void setProperty(String string, Object obj) {
        if(string.equalsIgnoreCase(CITY)) {
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
        final String stringToParse = stringBuffer.toString();
        System.out.println(stringToParse);
        int indexTemp = stringToParse.indexOf("temp");
        String tempString = stringToParse.substring(indexTemp + 6 );
        int index1 = tempString.indexOf(",") == -1? Integer.MAX_VALUE : tempString.indexOf(",");
        int index2 = tempString.indexOf("},") == -1? Integer.MAX_VALUE : tempString.indexOf("},");
        String temperatureString = tempString.substring(0, Math.min(index1, index2));

        final int temp = (int)Math.round(Double.parseDouble(temperatureString));

        final boolean finalTestSuccess = testSuccess;
        return new TestResult() {
            @Override
            public boolean isSuccess() {
                return finalTestSuccess;
            }

            @Override
            public String getName() {
                return "Temperature result";
            }

            @Override
            public String getObserverState() {
               return mapTemperature() ;
            }

            private String mapTemperature() {
                System.out.println("Map temperature "+temp);
                if(temp < 0) {
                    return "Freezing";
                }  else if(temp < 8) {
                    return "Cold";
                } else if(temp < 15) {
                    return "Mild";
                }  else if(temp < 25) {
                    return "Warm";
                }  else if(temp < 30) {
                    return "Hot";
                }
                return "Heat";
            };
        };
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String[] getSupportedStates() {
        return tempStates;
    }

    public static void main(String[] args){
        TemperatureSensor weatherSensor = new TemperatureSensor();
        weatherSensor.setProperty("city", "London");
        TestResult testResult = weatherSensor.execute(null);
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
