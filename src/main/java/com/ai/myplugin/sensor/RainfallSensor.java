/**
 * Created by User: veselin
 * On Date: 20/03/14
 */

package com.ai.myplugin.sensor;

import com.ai.api.SensorPlugin;
import com.ai.api.SensorResult;
import com.ai.api.SessionContext;
import com.ai.myplugin.util.EmptyTestResult;
import com.ai.myplugin.util.Rest;
import com.ai.myplugin.util.Utils;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import twitter4j.internal.org.json.JSONArray;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


@PluginImplementation
public class RainfallSensor implements SensorPlugin {
    protected static final Log log = LogFactory.getLog(RainfallSensor.class);
    static final String LOCATION = "location";
    static final String LATITUDE = "latitude";
    static final String LONGITUDE = "longitude";
    static final String RUNTIME_LATITUDE = "runtime_latitude";
    static final String RUNTIME_LONGITUDE = "runtime_longitude";
    private static final String CLEAR = "Clear";
    private static final String RAIN = "Rain";
    private static final String HEAVY_RAIN = "Heavy Rain";
    private static final String STORM = "Storm";
    //http://gps.buienradar.nl/getrr.php?lat=52&lon=4
    String url = "http://gps.buienradar.nl/getrr.php?";
    Map<String, Object> propertiesMap = new ConcurrentHashMap<String, Object>();

    @Override
    public SensorResult execute(SessionContext testSessionContext) {
        log.info("execute "+ getName() + ", sensor type:" +this.getClass().getName());
        Map<Double, Double> map;
        try {
            map = Utils.getLocation(testSessionContext, getProperty(LOCATION),
                    getProperty(LONGITUDE), getProperty(LATITUDE));
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            return new EmptyTestResult();
        }
        Double latitude = (Double) map.keySet().toArray()[0];
        Double longitude = (Double) map.values().toArray()[0];

        String pathURL =  url + "lat="+latitude + "&lon="+longitude;
        double avg = 0;
        double max = -1;
        double min = 255;
        final ArrayList<Double> list = new ArrayList<Double>();
        try {
            String stringToParse = Rest.httpGet(pathURL);
            log.info("stringToParse "+stringToParse);
            //000|10:20 000|10:25 000|10:30 000|10:35 000|10:40 000|10:45 000|10:50 000|10:55 000|11:00 000|11:05 000|11:10 000|11:15 000|11:20 000|11:25 000|11:30 000|11:35 000|11:40 000|11:45 000|11:50 000|11:55 000|12:00 000|12:05 000|12:10 000|12:15 000|12:20
            /*
            Op basis van lat lon coördinaten kunt u de neerslag twee uur vooruit ophalen in tekst vorm. 0 is droog, 255 is zware regen.
mm/per uur = 10^((waarde -109)/32)
Dus 77 = 0.1 mm/uur
             */
            StringTokenizer stringTokenizer = new StringTokenizer(stringToParse, "|");
            if(stringTokenizer.countTokens() < 2)
                throw new Exception("there is nothing to parse "+stringToParse);
            //TODO check how to do it better, now only the aggregate for the next hour
            double temp = -1;
            int count = 0;
            while(stringTokenizer.hasMoreTokens()){
                //double temp = Utils.getDouble(stringTokenizer.nextToken().split("|")[0]);
                String tempString = stringTokenizer.nextToken();
                if(tempString.length() == 3)
                    temp  = Utils.getDouble(tempString);
                else if(tempString.length() == 8)
                    temp  = Utils.getDouble(tempString.substring(5));
                else
                continue;
                list.add(temp);

                if(min > temp)
                    min = temp;
                if(max < temp)
                    max = temp;
                avg += temp;
                count ++;
            }
            avg = avg/count;

        } catch (Exception e) {
            e.printStackTrace();
            log.error("error in getting the data from " + pathURL + ", "+e.getMessage());
            return new EmptyTestResult();
        }

        final double finalMin = min;
        final double finalMax = max;
        final double finalAvg = avg;
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
                return parseData(finalMin, finalMax, finalAvg);
            }

            @Override
            public List<Map<String, Number>> getObserverStates() {
                return null;
            }

            @Override
            public String getRawData() {
                JSONObject jsonObject = new JSONObject();
                JSONArray jsonArray = new JSONArray();
                int time = 0;
                for(Double d : list){
                    JSONObject jsonObject1 = new JSONObject();
                    jsonObject1.put(Integer.toString(time),d);
                    jsonArray.put(jsonObject1);
                    time +=5;
                }
                jsonObject.put("min", finalMin);
                jsonObject.put("max", finalMax);
                jsonObject.put("avg", finalAvg);
                jsonObject.put("mm_per_hour", computeRainMM(finalAvg));
                jsonObject.put("forecast_raw", jsonArray);
                return jsonObject.toJSONString();
            }
        } ;
    }

    /*
    mm/per uur = 10^((waarde -109)/32)
    Dus 77 = 0.1 mm/uur
    10^((77 -109)/32)
     */
    private double computeRainMM(double finalAvg) {
        log.info("computeRain in mm/hour from "+finalAvg);
        double calc = Math.pow(10, ((finalAvg -109)/32));
        DecimalFormat twoDForm = new DecimalFormat("#.00");
        return Double.valueOf(twoDForm.format(calc));
    }

    private String parseData(double min, double max, double avg){
        log.debug("parseData avg="+avg+ ", min="+min + ", max="+max);
        if(avg == 0) {
            return CLEAR;
        } else if(avg < 50) {
            return RAIN;
        }  else if(avg < 100) {
            return HEAVY_RAIN;
        }
        return STORM;
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
        return new String[] {CLEAR, RAIN, HEAVY_RAIN, STORM};
    }

    @Override
    public String[] getRequiredProperties() {
        return new String[] {LOCATION, LONGITUDE, LATITUDE};
    }

    @Override
    public String[] getRuntimeProperties() {
        return new String[] {RUNTIME_LATITUDE, RUNTIME_LONGITUDE};
    }

    @Override
    public void setProperty(String s, Object o) {
        if(Arrays.asList(getRequiredProperties()).contains(s)) {
            propertiesMap.put(s, o);
        } else {
            throw new RuntimeException("Property "+ s + " not in the required settings");
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

    public static void main(String []args) throws ParseException {
        RainfallSensor rainfallSensor = new RainfallSensor();
        rainfallSensor.setProperty(LOCATION, "Gent");
        SensorResult testResult = rainfallSensor.execute(new SessionContext(1));
        System.out.println(testResult.getObserverState());
        System.out.println(testResult.getRawData());
    }
}
