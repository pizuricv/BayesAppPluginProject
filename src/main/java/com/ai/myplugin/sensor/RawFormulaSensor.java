/**
 * Created with IntelliJ IDEA.
 * User: pizuricv
 */

package com.ai.myplugin.sensor;

import com.ai.bayes.plugins.BNSensorPlugin;
import com.ai.bayes.scenario.TestResult;
import com.ai.myplugin.util.EmptyTestResult;
import com.ai.myplugin.util.FormulaParser;
import com.ai.myplugin.util.Utils;
import com.ai.util.resource.NodeSessionParams;
import com.ai.util.resource.TestSessionContext;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


@PluginImplementation
public class RawFormulaSensor implements BNSensorPlugin {
    private static final Log log = LogFactory.getLog(RawFormulaSensor.class);

    private final String THRESHOLD = "threshold";
    private final String FORMULA = "formula";
    private Map deltaMap = new ConcurrentHashMap();

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

    private Double executeFormula(String formula) throws Exception {
        log.debug("executeFormula(" + formula + ")");
        return FormulaParser.executeFormula(formula);
    }


    @Override
    public String getDescription() {
        return "Parse raw data from the scenario context";
    }

    @Override
    public TestResult execute(TestSessionContext testSessionContext) {
        log.info("execute "+ getName() + ", sensor type:" +this.getClass().getName());
        double res = 0;
        String parseFormula = (String) getProperty(FORMULA);
        log.debug("Formula to parse: "+parseFormula);
        if(parseFormula.indexOf("dt") > -1) {
            Long delta = (Long) deltaMap.get("prevTime");
            if(delta == null)   {
                deltaMap.put("prevTime", System.currentTimeMillis()/1000);
                return new EmptyTestResult();
            }
            Long currentTime = System.currentTimeMillis()/1000;
            deltaMap.put("prevTime", currentTime);
            parseFormula.replaceAll("dt", Long.toBinaryString(currentTime - delta));
        }

        boolean success = false;
        try {
            parseFormula = FormulaParser.parse((Map<String, Object>) testSessionContext.getAttribute(NodeSessionParams.RAW_DATA), parseFormula) ;
            log.debug("Formula to parse after processing: "+parseFormula);
            res = executeFormula(parseFormula);
            success = true;
        } catch (Exception e) {
            log.error(e.getLocalizedMessage() + ", for formula: "+ parseFormula);
        }
        final double finalRes = res;
        if(!success)
            return new EmptyTestResult();
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
                    return "Equal";
                if(finalRes > Utils.getDouble(getProperty(THRESHOLD)))
                    return "Above";
                else
                    return "Below";
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
    public String getName() {
        return NAME;
    }

    @Override
    public String[] getSupportedStates() {
        return new String[] {"Above", "Equal", "Below",};
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
        testSessionContext.setAttribute(NodeSessionParams.RAW_DATA, mapTestResult);

        rawFormulaSensor.setProperty("formula", "GOOG->price - GOOG->moving_average");
        rawFormulaSensor.setProperty("threshold", 100);
        testResult = rawFormulaSensor.execute(testSessionContext);
        log.debug(testResult.getObserverState());
        log.debug(testResult.getRawData());


        rawFormulaSensor.setProperty("formula", "GOOG->price");
        rawFormulaSensor.setProperty("threshold", 100);
        testResult = rawFormulaSensor.execute(testSessionContext);
        log.debug(testResult.getObserverState());
        log.debug(testResult.getRawData());


    }

    @Override
    public void shutdown(TestSessionContext testSessionContext) {
        log.debug("Shutdown : " + getName() + ", sensor : "+this.getClass().getName());
    }
}
