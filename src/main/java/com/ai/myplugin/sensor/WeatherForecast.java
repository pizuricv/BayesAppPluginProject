/**
 * User: pizuricv
 * Date: 7/15/13
 */

package com.ai.myplugin.sensor;

import com.ai.api.*;
import com.ai.myplugin.util.APIKeys;
import com.ai.myplugin.util.EmptySensorResult;
import com.ai.myplugin.util.Rest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

//currently not exposed, TODO check if that one should be removed later
//@PluginImplementation
public class WeatherForecast implements SensorPlugin {

    private static final Logger log = LoggerFactory.getLogger(RawFormulaSensor.class);

    static final String server = "https://george-vustrey-weather.p.mashape.com/api.php?_method=getForecasts&location=";
    static final String CITY = "city";
    String city;
    String [] states = WeatherAbstractSensor.weatherStates;
    private static final String NAME = "WeatherForecast";



    @Override
    public Map<String,PropertyType> getRequiredProperties() {
        Map<String, PropertyType> map = new HashMap<>();
        map.put(CITY, new PropertyType(DataType.STRING, true, false));
        return map;
    }

    @Override
    public Map<String, PropertyType> getRuntimeProperties() {
        return new HashMap<>();
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
    public SensorResult execute(SessionContext testSessionContext) {
        log.info("execute "+ getName() + ", sensor type:" +this.getClass().getName());
        Map<String, String> map = new ConcurrentHashMap<String, String>();
        map.put("X-Mashape-Authorization", APIKeys.getMashapeKey());
        try {
            String str = Rest.httpGet(server + city, map).body();
            log.debug(str);
        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
            e.printStackTrace();
        }
        return new EmptySensorResult();
    }

    @Override
    public String getName() {
        return "WeatherForecast";
    }

    @Override
    public Set<String> getSupportedStates() {
        return new HashSet(Arrays.asList(states));
    }

    @Override
    public void setup(SessionContext testSessionContext) {
        log.debug("Setup : " + getName() + ", sensor : "+this.getClass().getName());
    }

    @Override
    public void shutdown(SessionContext testSessionContext) {
        log.debug("Shutdown : " + getName() + ", sensor : "+this.getClass().getName());
    }

    public static void main(String []args){
        WeatherForecast weatherForecast = new WeatherForecast();
        weatherForecast.setProperty("city","London");
        weatherForecast.execute(null);
    }
}
