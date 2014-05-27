/**
 * Created by User: veselin
 * On Date: 20/03/14
 */

package com.ai.myplugin.sensor;

import com.ai.api.*;
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

    //http://gps.buienradar.nl/getrr.php?lat=52&lon=4
    private static final String BASE_URL = "http://gps.buienradar.nl/getrr.php?";

    Map<String, Object> propertiesMap = new ConcurrentHashMap<String, Object>();

    @Override
    public SensorResult execute(SessionContext testSessionContext) {
        log.info("execute "+ getName() + ", sensor type:" +this.getClass().getName());
        LatLng latLng;
        try {
            latLng = Utils.getLocation(testSessionContext, getProperty(LOCATION), getProperty(LONGITUDE), getProperty(LATITUDE));
        } catch (RuntimeException e) {
            log.error("error in getting the location: " + e.getMessage(), e);
            return new EmptySensorResult();
        }
        String pathURL = BASE_URL + "lat="+latLng.latitude + "&lon="+latLng.longitude;

        String stringToParse;
        try {
            stringToParse = Rest.httpGet(pathURL).body();
        } catch (IOException e) {
            log.error("error in getting the data from " + pathURL + ", " + e.getMessage(), e);
            return new EmptySensorResult();
        }

        final Optional<RainResult> parsed = parseResponse(stringToParse);

        return new SensorResult() {
            @Override
            public boolean isSuccess() {
                return true;
            }

            @Override
            public String getName() {
                return "RainfallSensor test result";
            }

            @Override
            public String getObserverState() {
                return parsed.map(RainResult::evaluate).orElse(null);
            }

            @Override
            public List<Map<String, Number>> getObserverStates() {
                return Collections.emptyList();
            }

            @Override
            public String getRawData() {
                return parsed.map(r -> resultToJson(r).toString()).orElse(null);
            }
        };
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
        return "RainfallSensor";
    }

    @Override
    public String[] getSupportedStates() {
        return new String[] {STATE_CLEAR, STATE_RAIN, STATE_HEAVY_RAIN, STATE_STORM};
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


    Optional<RainResult> parseResponse(String stringToParse){
        double avg = 0;
        double max = -1;
        double min = 255;
        log.debug("stringToParse " + stringToParse);
        //000|10:20 000|10:25 000|10:30 000|10:35 000|10:40 000|10:45 000|10:50 000|10:55 000|11:00 000|11:05 000|11:10 000|11:15 000|11:20 000|11:25 000|11:30 000|11:35 000|11:40 000|11:45 000|11:50 000|11:55 000|12:00 000|12:05 000|12:10 000|12:15 000|12:20
            /*
            Op basis van lat lon coördinaten kunt u de neerslag twee uur vooruit ophalen in tekst vorm. 0 is droog, 255 is zware regen.
            mm/per uur = 10^((waarde -109)/32)
            Dus 77 = 0.1 mm/uur
             */
        StringTokenizer stringTokenizer = new StringTokenizer(stringToParse, "\n");
        if(stringTokenizer.countTokens() < 2)
            throw new RuntimeException("there is nothing to parse " + stringToParse);
        //TODO check how to do it better, now only the aggregate for the next hour
        double temp = -1;
        int count = 0;
        final List<Double> list = new ArrayList<>();
        while(stringTokenizer.hasMoreTokens()){
            //double temp = Utils.getDouble(stringTokenizer.nextToken().split("|")[0]);
            String tempString = stringTokenizer.nextToken();
            if(tempString.length() == 3) {
                temp = Utils.getDouble(tempString);
            } else if(tempString.length() == 8) {
                temp = Utils.getDouble(tempString.substring(5));
            } else {
                // when we get a time without result
                continue;
            }
            list.add(temp);

            if(min > temp)
                min = temp;
            if(max < temp)
                max = temp;
            avg += temp;
        }
        if(list.size() == 0){
            return Optional.empty();
        }else {
            avg = avg / list.size();
            return Optional.of(new RainResult(min, max, avg, list));
        }
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
        for(Double d : result.results){
            JSONObject jsonObject1 = new JSONObject();
            jsonObject1.put(Integer.toString(time),d);
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

    static class RainResult{
        final double min;
        final double max;
        final double avg;
        final List<Double> results;

        RainResult(final double min, final double max, final double avg, final List<Double> results) {
            this.min = min;
            this.max = max;
            this.avg = avg;
            this.results = results;
        }

        String evaluate(){
            log.debug("evaluate " + this);
            if(avg == 0) {
                return STATE_CLEAR;
            } else if(avg < 50) {
                return STATE_RAIN;
            } else if(avg < 100) {
                return STATE_HEAVY_RAIN;
            } else {
                return STATE_STORM;
            }
        }

        @Override
        public String toString() {
            return "RainResult{" +
                    "min=" + min +
                    ", max=" + max +
                    ", avg=" + avg +
                    ", results=" + results +
                    '}';
        }
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
}
