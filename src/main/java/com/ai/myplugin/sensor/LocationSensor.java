package com.ai.myplugin.sensor;

import com.ai.bayes.plugins.BNSensorPlugin;
import com.ai.bayes.scenario.TestResult;
import com.ai.myplugin.util.EmptyTestResult;
import com.ai.myplugin.util.Mashape;
import com.ai.myplugin.util.Rest;
import com.ai.util.resource.TestSessionContext;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by User: veselin
 * On Date: 26/12/13
 */
@PluginImplementation
public class LocationSensor implements BNSensorPlugin{
    protected static final Log log = LogFactory.getLog(LocationSensor.class);
    static final String LOCATION = "location";
    static final String LATITUDE = "latitude";
    static final String LONGITUDE = "longitude";
    static final String DISTANCE = "distance";
    String location;
    String latitudeCoordinate;
    String longitudeCoordinate;
    Double distance;
    Map<String, Object> propertiesMap = new ConcurrentHashMap<String, Object>();

    String [] states = {"Within", "Out"};
    private static final String NAME = "LocationSensor";

    @Override
    public String[] getRequiredProperties() {
        return new String[]{LOCATION, LATITUDE, LONGITUDE, DISTANCE};
    }

    @Override
    public String[] getRuntimeProperties() {
        return new String[]{};
    }

    @Override
    public void setProperty(String string, Object obj) {
        if(Arrays.asList(getRequiredProperties()).contains(string)) {
            propertiesMap.put(string, obj);
        } else {
            throw new RuntimeException("Property "+ string + " not in the required settings");
        }
    }

    @Override
    public Object getProperty(String string) {
        return propertiesMap.get(string);
    }

    @Override
    public String getDescription() {
        return "Checks whether a collected data is within a distance from a given location";
    }

    @Override
    public TestResult execute(TestSessionContext testSessionContext) {
        log.info("execute "+ getName() + ", sensor type:" +this.getClass().getName());
        if(getProperty(DISTANCE) == null)
            throw new RuntimeException("distance not set");
        Map<String, String> map = new ConcurrentHashMap<String, String>();
        map.put("X-Mashape-Authorization", Mashape.getKey());
        if(getProperty(LOCATION) != null){
            try {
                final String str = Rest.httpGet(LocationRawSensor.server + location, map);
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                log.error(e.getLocalizedMessage());
                return new EmptyTestResult();
            }

        }   else if (getProperty(LONGITUDE)!= null && getProperty(LATITUDE)!= null){
            return null;

        }
        else
            throw new RuntimeException("location not properly set");
    }

    @Override
    public void shutdown(TestSessionContext testSessionContext) {

    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String[] getSupportedStates() {
        return states;
    }
}
