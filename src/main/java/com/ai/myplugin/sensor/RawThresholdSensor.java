/**
 * Created with IntelliJ IDEA.
 * User: pizuricv
 */

package com.ai.myplugin.sensor;

import com.ai.api.*;
import com.ai.myplugin.util.EmptySensorResult;
import com.ai.myplugin.util.FormulaParser;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import java.util.*;

@PluginImplementation
public class RawThresholdSensor implements SensorPlugin {
    private static final Log log = LogFactory.getLog(RawFormulaSensor.class);
    FormulaParser formulaParser = new FormulaParser();

    private ArrayList<Long> threshold = new ArrayList<Long>();
    private ArrayList<String> states = new ArrayList<String>();
    //in case that states are defined via property
    private ArrayList<String> definedStates  = new ArrayList<String>();
    private static final String NAME = "RawThresholdSensor";
    private String rawData;
    private String node;

    @Override
    public Map<String, PropertyType> getRequiredProperties() {
        Map<String, PropertyType> map = new HashMap<>();
        map.put("threshold", new PropertyType(DataType.DOUBLE, true, false));
        map.put("states", new PropertyType(DataType.STRING, true, false));
        map.put("rawData", new PropertyType(DataType.STRING, true, false));
        map.put("node", new PropertyType(DataType.STRING, true, false));
        return map;
    }

    @Override
    public Map<String,PropertyType> getRuntimeProperties() {
        return new HashMap<>();
    }


    //comma separated list of thresholds
    @Override
    public void setProperty(String s, Object o) {
        if("threshold".endsWith(s)){
            if(o instanceof String)  {
                String input = (String) o;
                input = input.replace("[","").replace("]","");
                StringTokenizer stringTokenizer = new StringTokenizer(input, ",");
                int i = 0;
                states.add("level_"+ i++);
                while(stringTokenizer.hasMoreElements()){
                    threshold.add(Long.parseLong(stringTokenizer.nextToken().trim()));
                    states.add("level_"+ i++);
                }
            } else {
                threshold.add((Long) o);
                states.add("level_0");
                states.add("level_1");
            }
            Collections.reverse(threshold);
        } else if ("rawData".equals(s)){
            rawData = o.toString();
        } else if ("node".equals(s)){
            node = o.toString();
        } else if("states".endsWith(s)){
            if(o instanceof String)  {
                String input = (String) o;
                input = input.replace("[","").replace("]","").replaceAll("\"","");
                StringTokenizer stringTokenizer = new StringTokenizer(input, ",");
                while(stringTokenizer.hasMoreElements())
                    definedStates.add(stringTokenizer.nextToken().trim());
            }
        }
    }

    @Override
    public Object getProperty(String s) {
        if("threshold".endsWith(s)){
            return threshold;
        } else if("rawData".endsWith(s)){
            return rawData;
        } else if("node".endsWith(s)){
            return node;
        }  else if("states".endsWith(s)){
            return states;
        }
        else{
            throw new RuntimeException("Property " + s + " not recognised by " + getName());
        }
    }

    @Override
    public String getDescription() {
        return "Parse raw data from the scenario context";
    }

    @Override
    public SensorResult execute(SessionContext testSessionContext) {
        log.info("execute "+ getName() + ", sensor type:" +this.getClass().getName());
        Map<String, Object> mapTestResult = (Map<String, Object>) testSessionContext.getAttribute(SessionParams.RAW_DATA);
        if(mapTestResult == null){
            log.debug("no map found");
            return new EmptySensorResult();
        }

        JSONObject jsonObject = (JSONObject) (mapTestResult.get(node));
        if(jsonObject == null)
            return new EmptySensorResult();
        /*
        final Object value;
        try {
            value = ((JSONObject) new JSONParser().parse((String) jsonObject.get("rawData"))).get(rawData);
        } catch (ParseException e) {
            e.printStackTrace();
            log.error(e.getLocalizedMessage());
            return new EmptyTestResult();
        }
        final Double dataD = Utils.getDouble(value);   */
        final Double dataD;
        try {
            String parseFormula = formulaParser.parseFormula("<"+node+".rawData."+rawData+">",
                    (Map<String, Object>) testSessionContext.getAttribute(SessionParams.RAW_DATA)) ;
            dataD =  FormulaParser.executeFormula(parseFormula);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getLocalizedMessage());
            return new EmptySensorResult();
        }

        return new SensorResult() {
            @Override
            public boolean isSuccess() {
                return true;
            }

            @Override
            public String getName() {
                return "Raw Data Sensor Result";
            }

            @Override
            public String getObserverState() {
                return mapResult(dataD);
            }

            @Override
            public List<Map<String, Number>> getObserverStates() {
                return null;
            }

            @Override
            public String getRawData() {
                return "{" +
                        "\"" + rawData + "\" : " + dataD +
                        "}";
            }

        };
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String[] getSupportedStates() {
        if(definedStates.size() == 0)
            return states.toArray(new String[states.size()]);
        else
            return definedStates.toArray(new String[definedStates.size()]);
    }

    private String mapResult(Double result) {
        if(definedStates.size() == 0){
            int i = states.size() - 1;
            for(Long l : threshold){
                if(result  > l){
                    return "level_" + i;
                }
                i --;
            }
            return "level_0";
        } else {
            int i = definedStates.size() - 1;
            for(Long l : threshold){
                if(result  > l){
                    return definedStates.get(i);
                }
                i --;
            }
            return definedStates.get(0);
        }
    }

    @Override
    public void setup(SessionContext testSessionContext) {
        log.debug("Setup : " + getName() + ", sensor : "+this.getClass().getName());
    }

    @Override
    public void shutdown(SessionContext testSessionContext) {
        log.debug("Shutdown : " + getName() + ", sensor : "+this.getClass().getName());
        formulaParser.restStats();
    }

    public static void main(String []args){
        WeatherSensor weatherSensor = new WeatherSensor();
        weatherSensor.setProperty("city", "GB, London");
        SensorResult testResult = weatherSensor.execute(null);
        log.info(testResult.getObserverState());
        log.info(testResult.getRawData());
        //this is injected by scenario
        RawThresholdSensor rawThresholdSensor = new RawThresholdSensor();
        rawThresholdSensor.setProperty("rawData", "temperature");
        rawThresholdSensor.setProperty("threshold", "5,10,15,25,35");
        rawThresholdSensor.setProperty("node", "node1");
        SessionContext testSessionContext = new SessionContext(1);
        Map<String, Object> mapTestResult = new HashMap<String, Object>();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("rawData", testResult.getRawData());
        mapTestResult.put("node1", jsonObject);
        testSessionContext.setAttribute(SessionParams.RAW_DATA, mapTestResult);
        testResult = rawThresholdSensor.execute(testSessionContext);
        log.info(testResult.getObserverState());
        log.info(testResult.getRawData());


        rawThresholdSensor = new RawThresholdSensor();
        rawThresholdSensor.setProperty("rawData", "temperature");
        rawThresholdSensor.setProperty("threshold", "5,15,25");
        rawThresholdSensor.setProperty("states", "[low, medium, high, heat]");
        rawThresholdSensor.setProperty("node", "node1");
        testResult = rawThresholdSensor.execute(testSessionContext);
        log.info(testResult.getObserverState());
        log.info(testResult.getRawData());
    }
}
