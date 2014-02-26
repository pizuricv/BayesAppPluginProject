/**
 * User: pizuricv
 * Date: 7/15/13
 */

package com.ai.myplugin.sensor;

import com.ai.bayes.plugins.BNSensorPlugin;
import com.ai.bayes.scenario.TestResult;
import com.ai.myplugin.util.APIKeys;
import com.ai.myplugin.util.EmptyTestResult;
import com.ai.myplugin.util.Rest;
import com.ai.util.resource.TestSessionContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.URLEncoder;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//currently not exposed, TODO check if that one should be removed later
//@PluginImplementation
public class WeatherForecast implements BNSensorPlugin {
    private static final Log log = LogFactory.getLog(WeatherForecast.class);

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
    public String[] getRuntimeProperties() {
        return new String[]{};
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
        log.info("execute "+ getName() + ", sensor type:" +this.getClass().getName());
        Map<String, String> map = new ConcurrentHashMap<String, String>();
        map.put("X-Mashape-Authorization", APIKeys.getMashapeKey());
        try {
            String str = Rest.httpGet(server + city, map);
            log.debug(str);
        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
            e.printStackTrace();
        }
        return new EmptyTestResult();
    }

    @Override
    public String getName() {
        return "WeatherForecast";
    }

    @Override
    public String[] getSupportedStates() {
        return states;
    }

    @Override
    public void shutdown(TestSessionContext testSessionContext) {
        log.debug("Shutdown : " + getName() + ", sensor : "+this.getClass().getName());
    }

    public static void main(String []args){
        WeatherForecast weatherForecast = new WeatherForecast();
        weatherForecast.setProperty("city","London");
        weatherForecast.execute(null);
    }
}
