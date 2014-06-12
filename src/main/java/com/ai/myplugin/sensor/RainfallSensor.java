/**
 * Created by User: veselin
 * On Date: 20/03/14
 */

package com.ai.myplugin.sensor;

import com.ai.api.*;
import com.ai.myplugin.services.BuienradarService;
import com.ai.myplugin.services.RainResult;
import com.ai.myplugin.util.*;
import net.xeoh.plugins.base.annotations.PluginImplementation;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.internal.org.json.JSONArray;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


@PluginImplementation
@PluginHeader(version = "1.0.1", author = "Veselin", category = "Weather", iconURL = "http://app.waylay.io/icons/radar_weather.png")
public class RainfallSensor implements SensorPlugin {

    private static final Logger log = LoggerFactory.getLogger(RainfallSensor.class);

    static final String LOCATION = "location";
    private static final String LATITUDE = "latitude";
    private static final String LONGITUDE = "longitude";
    private static final String RUNTIME_LATITUDE = "runtime_latitude";
    private static final String RUNTIME_LONGITUDE = "runtime_longitude";

    static final String STATE_CLEAR = "Clear";
    static final String STATE_RAIN = "Rain";
    static final String STATE_HEAVY_RAIN = "Heavy Rain";
    static final String STATE_STORM = "Storm";
    private static final String NAME = "WeatherRadar";

    Map<String, Object> propertiesMap = new ConcurrentHashMap<String, Object>();
    private String[] states = {STATE_CLEAR, STATE_RAIN, STATE_HEAVY_RAIN, STATE_STORM};

    @Override
    public SensorResult execute(SessionContext testSessionContext) {
        log.info("execute "+ getName() + ", sensor type:" +this.getClass().getName());
        LatLng latLng;
        try {
            latLng = Utils.getLocation(testSessionContext, getProperty(LOCATION), getProperty(LONGITUDE), getProperty(LATITUDE));
        } catch (RuntimeException e) {
            log.error("error in getting the location: " + e.getMessage(), e);
            return SensorResultBuilder.failure().build();
        }

        BuienradarService service = new BuienradarService();
        Optional<RainResult> result;
        try {
            result = service.fetch(latLng);
        } catch (IOException e) {
            log.error("error in getting the rainfall data: " + e.getMessage(), e);
            return SensorResultBuilder.failure().build();
        }
        Optional<RainResult> finalResult = result;

        return SensorResultBuilder
                .success()
                .withObserverState(finalResult.map(r -> evaluate(r)).orElse(null))
                .withRawData(finalResult.map(r -> resultToJson(r).toString()).orElse(null))
                .build();

    }

    @Override
    public void setup(SessionContext testSessionContext) {
        log.debug("Setup : " + getName() + ", sensor : "+this.getClass().getName());
    }

    @Override
    public void shutdown(SessionContext testSessionContext) {
        
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Set<String> getSupportedStates() {
        return new HashSet(Arrays.asList(states));
    }

    @Override
    public Map<String, PropertyType> getRequiredProperties() {
        Map<String, PropertyType> map = new HashMap<>();
        map.put(LOCATION, new PropertyType(DataType.STRING, true, false));
        map.put(LATITUDE, new PropertyType(DataType.DOUBLE, true, false));
        map.put(LONGITUDE, new PropertyType(DataType.DOUBLE, true, false));
        return map;
    }

    @Override
    public Map<String, PropertyType> getRuntimeProperties() {
        Map<String, PropertyType> map = new HashMap<>();
        map.put(RUNTIME_LATITUDE, new PropertyType(DataType.STRING, true, false));
        map.put(RUNTIME_LONGITUDE, new PropertyType(DataType.DOUBLE, true, false));
        return map;
    }

    @Override
    public void setProperty(String s, Object o) {
        if(getRequiredProperties().keySet().contains(s)) {
            propertiesMap.put(s, o);
        } else {
            throw new RuntimeException("Property " + s + " not in the required settings");
        }
    }

    @Override
    public Object getProperty(String string) {
        return propertiesMap.get(string);
    }

    @Override
    public String getDescription() {
        return "Short weather forecast based on radar forecast";
    }




    @Override
    public List<RawDataType> getRawDataTypes() {
        List<RawDataType> list = new ArrayList<>();
        list.add(new RawDataType("min", "double", DataType.DOUBLE, true, CollectedType.COMPUTED));
        list.add(new RawDataType("max", "double", DataType.DOUBLE, true, CollectedType.COMPUTED));
        list.add(new RawDataType("avg", "double", DataType.DOUBLE, true, CollectedType.COMPUTED));
        list.add(new RawDataType("mm_per_hour", "double", DataType.DOUBLE, true, CollectedType.COMPUTED));
        list.add(new RawDataType("forecast_raw", "double", DataType.DOUBLE, true, CollectedType.COMPUTED));
        return list;
    }

    private JSONObject resultToJson(RainResult result){
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        int time = 0;
        for(RainResult.RainAmount amount : result.results){
            JSONObject jsonObject1 = new JSONObject();
            jsonObject1.put(Integer.toString(time), amount.amount.orElse(null));
            jsonArray.put(jsonObject1);
            time +=5;
        }
        jsonObject.put("min", result.min);
        jsonObject.put("max", result.max);
        jsonObject.put("avg", result.avg);
        jsonObject.put("mm_per_hour", computeRainMM(result.avg));
        jsonObject.put("forecast_raw", jsonArray);
        return jsonObject;
    }

    /*
        mm/per uur = 10^((waarde -109)/32)
        Dus 77 = 0.1 mm/uur
        10^((77 -109)/32)
    */
    private double computeRainMM(double finalAvg) {
        log.info("computeRain in mm/hour from " + finalAvg);
        double calc = Math.pow(10, ((finalAvg -109)/32));
        DecimalFormat twoDForm = new DecimalFormat("#.00");
        return Double.valueOf(twoDForm.format(calc));
    }

    static String evaluate(RainResult rainResult){
        RainfallSensor.log.debug("evaluate " + rainResult);
        if(rainResult.avg == 0) {
            return RainfallSensor.STATE_CLEAR;
        } else if(rainResult.avg < 50) {
            return RainfallSensor.STATE_RAIN;
        } else if(rainResult.avg < 100) {
            return RainfallSensor.STATE_HEAVY_RAIN;
        } else {
            return RainfallSensor.STATE_STORM;
        }
    }
}
