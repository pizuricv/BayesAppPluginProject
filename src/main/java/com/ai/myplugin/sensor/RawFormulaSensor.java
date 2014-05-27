/**
 * Created with IntelliJ IDEA.
 * User: pizuricv
 */

package com.ai.myplugin.sensor;

import com.ai.api.*;
import com.ai.myplugin.util.EmptySensorResult;
import com.ai.myplugin.util.FormulaParser;
import com.ai.myplugin.util.Utils;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


@PluginImplementation
@PluginHeader(version = "1.0.1", author = "Veselin", category = "Event Processing", iconURL = "http://app.waylay.io/icons/formula.png")
public class RawFormulaSensor implements SensorPlugin {
    private static final Log log = LogFactory.getLog(RawFormulaSensor.class);
    FormulaParser formulaParser = new FormulaParser();

    private final String THRESHOLD = "threshold";
    private final String FORMULA = "formula";
    private Map deltaMap = new ConcurrentHashMap();
    // if threshold is given as a list, then we will create states as the range
    private ArrayList<String> configuredStates  = new ArrayList<String>();
    private ArrayList<Long> thresholds = new ArrayList<Long>();

    Map<String, Object> propertiesMap = new ConcurrentHashMap<String, Object>();

    private static final String NAME = "RawFormulaSensor";

    @Override
    public Map<String,PropertyType> getRequiredProperties() {
        Map<String, PropertyType> map = new HashMap<>();
        map.put(THRESHOLD, new PropertyType(DataType.DOUBLE, true, false));
        map.put(FORMULA, new PropertyType(DataType.STRING, true, false));
        return map;
    }

    @Override
    public Map<String,PropertyType> getRuntimeProperties() {
        return new HashMap<>();
    }

    public void setProperty(String string, Object obj) {
        if(getRequiredProperties().keySet().contains(string)) {
            if(string.equalsIgnoreCase(THRESHOLD)){
                String input = obj.toString();
                input = input.replace("[","").replace("]","");
                StringTokenizer stringTokenizer = new StringTokenizer(input, ",");
                if(stringTokenizer.countTokens() > 1){
                    int i = 0;
                    configuredStates.add("level_"+ i++);
                    while(stringTokenizer.hasMoreElements()){
                        thresholds.add(Long.parseLong(stringTokenizer.nextToken().trim()));
                        configuredStates.add("level_"+ i++);
                    }
                    Collections.reverse(thresholds);
                } else
                    propertiesMap.put(string, obj);
             }else
                propertiesMap.put(string, obj);
        } else {
            throw new RuntimeException("Property "+ string + " not in the required settings");
        }
    }

    public Object getProperty(String string) {
        return propertiesMap.get(string);
    }

    private Double executeFormula(String formula) throws Exception {
        log.debug("executeFormula(" + formula + ")");
        return FormulaParser.executeFormula(formula);
    }


    @Override
    public String getDescription() {
        return "Parse raw data from the scenario context";
    }

    @Override
    public SensorResult execute(SessionContext testSessionContext) {
        log.info("execute "+ getName() + ", sensor type:" +this.getClass().getName());
        String parseFormula = (String) getProperty(FORMULA);
        log.debug("Formula to parse: "+parseFormula);

        double res = 0;
        if(parseFormula.indexOf("dt") > -1) {
            Long prev = (Long) deltaMap.get("prevTime");
            if(prev == null)   {
                deltaMap.put("prevTime", System.currentTimeMillis()/1000);
                return new EmptySensorResult();
            }
            Long currentTime = System.currentTimeMillis()/1000;
            deltaMap.put("prevTime", currentTime);
            parseFormula = parseFormula.replaceAll("dt", Long.toString(currentTime - prev));
        }

        boolean success = false;
        try {
            parseFormula = formulaParser.parseFormula(parseFormula, (Map<String, Object>) testSessionContext.getAttribute(SessionParams.RAW_DATA)) ;
            log.info("Formula to parse after processing: "+parseFormula);
            res = executeFormula(parseFormula);
            success = true;
        } catch (Exception e) {
            log.error(e.getLocalizedMessage() + ", for formula: "+ parseFormula);
        }
        final double finalRes = res;
        if(!success)
            return new EmptySensorResult();
        else
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
                return mapResult(finalRes);
            }

            @Override
            public List<Map<String, Number>> getObserverStates() {
                return null;
            }

            @Override
            public String getRawData() {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("formulaValue", finalRes);
                return  jsonObject.toJSONString();
            }
        };
    }

    @Override
    public List<RawDataType> getRawDataTypes() {
        List<RawDataType> list = new ArrayList<>();
        list.add(new RawDataType("formulaValue", "double", DataType.DOUBLE, true, CollectedType.COMPUTED));
        return list;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String[] getSupportedStates() {
        if(configuredStates.size() == 0)
            return new String[] {"Above", "Equal", "Below",};
        else
            return configuredStates.toArray(new String[configuredStates.size()]);

    }

    private String mapResult(Double value) {
        if(configuredStates.size() == 0){
            if(value.equals(Utils.getDouble(getProperty(THRESHOLD))))
                return "Equal";
            if(value > Utils.getDouble(getProperty(THRESHOLD)))
                return "Above";
            else
                return "Below";
        } else {
            int i = configuredStates.size() - 1;
            for(Long l : thresholds){
                if(value  > l){
                    return configuredStates.get(i);
                }
                i --;
            }
            return configuredStates.get(0);
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

        Long time = System.currentTimeMillis()/1000;

        RawFormulaSensor rawFormulaSensor = new RawFormulaSensor();
        rawFormulaSensor.setProperty("formula", "<node1.rawData.value1> + <node2.rawData.value2>");

        rawFormulaSensor.setProperty("threshold", "4");
        SessionContext testSessionContext = new SessionContext(1);
        Map<String, Object> mapTestResult = new HashMap<String, Object>();
        JSONObject objRaw = new JSONObject();
        objRaw.put("value1", 1);
        objRaw.put("time", time);
        objRaw.put("rawData", objRaw.toJSONString());
        mapTestResult.put("node1", objRaw);

        objRaw = new JSONObject();
        objRaw.put("value2", 1);
        objRaw.put("time", time);
        objRaw.put("rawData", objRaw.toJSONString());
        mapTestResult.put("node2", objRaw);

        testSessionContext.setAttribute(SessionParams.RAW_DATA, mapTestResult);
        SensorResult testResult = rawFormulaSensor.execute(testSessionContext);
        log.debug(testResult.getObserverState());
        log.debug(testResult.getRawData());


        StockPriceSensor stockPriceSensor = new StockPriceSensor();
        stockPriceSensor.setProperty(StockPriceSensor.STOCK, "GOOG");
        stockPriceSensor.setProperty(StockPriceSensor.THRESHOLD, "800.0");
        testResult = stockPriceSensor.execute(testSessionContext);
        log.debug(testResult.getObserverState());
        log.debug(testResult.getRawData());
        JSONObject obj = new JSONObject();
        obj.put("time", time);
        obj.put("rawData", testResult.getRawData());
        mapTestResult = new ConcurrentHashMap<String, Object>();
        mapTestResult.put("GOOG", obj);
        testSessionContext.setAttribute(SessionParams.RAW_DATA, mapTestResult);

        rawFormulaSensor.setProperty("formula", "<GOOG.rawData.price> - <GOOG.rawData.moving_average>");
        rawFormulaSensor.setProperty("threshold", 100);
        testResult = rawFormulaSensor.execute(testSessionContext);
        log.debug(testResult.getObserverState());
        log.debug(testResult.getRawData());


        rawFormulaSensor.setProperty("formula", "<GOOG.rawData.price>");
        rawFormulaSensor.setProperty("threshold", 100);
        testResult = rawFormulaSensor.execute(testSessionContext);
        log.debug(testResult.getObserverState());
        log.debug(testResult.getRawData());


    }
}
