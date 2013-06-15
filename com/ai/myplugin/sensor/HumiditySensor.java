
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
public class HumiditySensor implements BNSensorPlugin{
    //TODO use JSON parsing later, need a tiny library for this
    String city;
    static final String server = "http://api.openweathermap.org/";

    String [] humidityStates = {"Low", "Normal", "High"};
    private static final String NAME = "Humidity";

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
        return "Humidity information";
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

        int indexHumidity = stringToParse.indexOf("humidity");
        String tempHumidity = stringToParse.substring(indexHumidity + 10);
        int index1 = tempHumidity.indexOf(",") == -1? Integer.MAX_VALUE : tempHumidity.indexOf(",");
        int index2 = tempHumidity.indexOf("},") == -1? Integer.MAX_VALUE : tempHumidity.indexOf("},");
        String humidityString = tempHumidity.substring(0, Math.min(index1, index2));

        final int humidity = (int) Double.parseDouble(humidityString);

        final boolean finalTestSuccess = testSuccess;
        return new TestResult() {
            @Override
            public boolean isSuccess() {
                return finalTestSuccess;
            }

            @Override
            public String getName() {
                return "Humidity result";
            }

            @Override
            public String getObserverState() {
                return mapHumidity();

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
        };
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String[] getSupportedStates() {
        return humidityStates;

    }

    public static void main(String[] args){
        HumiditySensor weatherSensor = new HumiditySensor();
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
