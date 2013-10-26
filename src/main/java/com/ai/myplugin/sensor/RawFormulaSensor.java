/**
 * Created with IntelliJ IDEA.
 * User: pizuricv
 */

package com.ai.myplugin.sensor;

import com.ai.bayes.plugins.BNSensorPlugin;
import com.ai.bayes.scenario.TestResult;
import com.ai.myplugin.util.Utils;
import com.ai.util.resource.NodeSessionParams;
import com.ai.util.resource.TestSessionContext;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.script.ScriptEngineManager;
import javax.script.ScriptEngine;
import javax.script.ScriptException;


import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


@PluginImplementation
public class RawFormulaSensor implements BNSensorPlugin {

    private final String THRESHOLD = "threshold";
    private final String FORMULA = "formula";
    ScriptEngineManager mgr = new ScriptEngineManager();
    ScriptEngine engine = mgr.getEngineByName("JavaScript");

    Map<String, Object> propertiesMap = new ConcurrentHashMap<String, Object>();

    private static final String NAME = "RawFormulaSensor";

    @Override
    public String[] getRequiredProperties() {
        return new String [] {THRESHOLD, FORMULA} ;
    }

    public void setProperty(String string, Object obj) {
        if(Arrays.asList(getRequiredProperties()).contains(string)) {
            propertiesMap.put(string, obj);
        } else {
            throw new RuntimeException("Property "+ string + " not in the required settings");
        }
    }

    public Object getProperty(String string) {
        return propertiesMap.get(string);
    }

    private Double executeFormula(String formula) throws ScriptException {
        return (Double) engine.eval(formula);
    }

    //formula in format node1->param1 OPER node=>param3 OPER node=>param3 ...
    private String parse(Map<String, Object>  attribute) throws ParseException {
        String returnString = ((String) getProperty(FORMULA)).replaceAll("\\(", " \\( ").replaceAll("\\)", " \\) ");
        String [] split = returnString.split("\\s+");
        Map<String, Double> map = new ConcurrentHashMap<String, Double>();
        for(String s1 : split)   {
            String [] s2 = s1.split("->");
            if(s2.length == 2)  {
                String node = s2[0];
                String value = s2[1];
                JSONObject jsonObject = (JSONObject) (attribute.get(node));
                Object rawValue =  ((JSONObject) new JSONParser().parse((String) jsonObject.get("rawData"))).get(value);
                map.put(s1, Utils.getDouble(rawValue));
            }
        }

        for(Map.Entry<String, Double> entry: map.entrySet()){
            returnString = returnString.replaceAll(entry.getKey() , entry.getValue().toString());
        }
        return returnString;
    };


    @Override
    public String getDescription() {
        return "Parse raw data from the scenario context";
    }

    @Override
    public TestResult execute(TestSessionContext testSessionContext) {
        System.out.println("execute "+ getName() + ", sensor type:" +this.getClass().getName());
        double res = 0;
        String parseFormula = (String) getProperty(FORMULA);
        System.out.println("Formula to parse: "+parseFormula);
        boolean success = false;
        try {
            parseFormula = parse((Map<String, Object>) testSessionContext.getAttribute(NodeSessionParams.RAW_DATA)) ;
            System.out.println("Formula to parse after processing: "+parseFormula);
            res = executeFormula(parseFormula);
            success = true;
        } catch (Exception e) {
            System.err.println(e.getLocalizedMessage() + ", for formula: "+ parseFormula);
        }
        final double finalRes = res;
        if(!success)
            return new EmptyResult();
        else
            return new TestResult() {
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
                if(finalRes == Utils.getDouble(getProperty(THRESHOLD)))
                    return "EQUAL";
                if(finalRes > Utils.getDouble(getProperty(THRESHOLD)))
                    return "ABOVE";
                else
                    return "BELOW";
            }

            @Override
            public List<Map<String, Number>> getObserverStates() {
                return null;
            }

            @Override
            public String getRawData() {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("value", finalRes);
                return  jsonObject.toJSONString();
            }

        };
    }


    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String[] getSupportedStates() {
        return new String[] {"ABOVE", "EQUAL", "BELOW",};
    }


    public static void main(String []args){

        Long time = System.currentTimeMillis()/1000;

        RawFormulaSensor rawFormulaSensor = new RawFormulaSensor();
        rawFormulaSensor.setProperty("formula", "node1->value1 + node2->value2");

        rawFormulaSensor.setProperty("threshold", "4");
        TestSessionContext testSessionContext = new TestSessionContext(1);
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

        testSessionContext.setAttribute(NodeSessionParams.RAW_DATA, mapTestResult);
        TestResult testResult = rawFormulaSensor.execute(testSessionContext);
        System.out.println(testResult.getObserverState());
        System.out.println(testResult.getRawData());


        StockPriceSensor stockPriceSensor = new StockPriceSensor();
        stockPriceSensor.setProperty(StockPriceSensor.STOCK, "GOOG");
        stockPriceSensor.setProperty(StockPriceSensor.THRESHOLD, "800.0");
        testResult = stockPriceSensor.execute(testSessionContext);
        System.out.println(testResult.getObserverState());
        System.out.println(testResult.getRawData());
        JSONObject obj = new JSONObject();
        obj.put("time", time);
        obj.put("rawData", testResult.getRawData());
        mapTestResult = new ConcurrentHashMap<String, Object>();
        mapTestResult.put("GOOG", obj);
        testSessionContext.setAttribute(NodeSessionParams.RAW_DATA, mapTestResult);

        rawFormulaSensor.setProperty("formula", "GOOG->price - GOOG->moving_average");
        rawFormulaSensor.setProperty("threshold", 100);
        testResult = rawFormulaSensor.execute(testSessionContext);
        System.out.println(testResult.getObserverState());
        System.out.println(testResult.getRawData());


    }

    private class EmptyResult implements TestResult {
        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public String getName() {
            return "";
        }

        @Override
        public String getObserverState() {
            return null;
        }

        @Override
        public List<Map<String, Number>> getObserverStates() {
            return null;
        }

        @Override
        public String getRawData() {
            return null;
        }
    }
}
