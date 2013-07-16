/**
 * User: pizuricv
 * Date: 7/15/13
 */

package com.ai.myplugin.sensor;

import com.ai.bayes.plugins.BNSensorPlugin;
import com.ai.bayes.scenario.TestResult;
import com.ai.myplugin.util.Mashape;
import com.ai.myplugin.util.Rest;
import com.ai.util.resource.TestSessionContext;

import java.net.URLEncoder;
import java.util.HashMap;

//@PluginImplementation
public class WeatherForecast implements BNSensorPlugin {

    static final String server = "https://george-vustrey-weather.p.mashape.com/api.php?_method=getForecasts&location=";
    static final String CITY = "city";
    String city;
    String [] states = WeatherAbstractSensor.weatherStates;
    private static final String NAME = "WeatherForecast";



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
        return "Weather Forecast";
    }

    @Override
    public TestResult execute(TestSessionContext testSessionContext) {

        HashMap<String, String> map = new HashMap<String, String>();
        map.put("X-Mashape-Authorization", Mashape.getKey());


        try {
            String str = Rest.httpGet(server + city, map);
            System.out.println(str);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String getName() {
        return "WeatherForecast";
    }

    @Override
    public String[] getSupportedStates() {
        return states;
    }

    public static void main(String []args){
        WeatherForecast weatherForecast = new WeatherForecast();
        weatherForecast.setProperty("city","London");
        weatherForecast.execute(null);
    }
}
