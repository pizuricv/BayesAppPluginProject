package com.ai.myplugin.sensor;

import com.ai.bayes.scenario.TestResult;
import com.ai.myplugin.util.FormulaParser;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by User: veselin
 * On Date: 29/10/13
 */
@PluginImplementation
public class StockFormulaSensor extends StockAbstractSensor {
    private static final Log log = LogFactory.getLog(StockFormulaSensor.class);

    @Override
    public String[] getRequiredProperties() {
        return new String[]{STOCK, THRESHOLD, FORMULA_DEFINITION};
    }

    @Override
    protected String getTag() {
        return StockAbstractSensor.FORMULA;
    }

    @Override
    protected String getSensorName() {
        return "StockFormulaSensor";
    }

    @Override
    //only used by StockFormulaSensor
    protected double getFormulaResult(ConcurrentHashMap<String, Double> hashMap) throws Exception{
        log.debug("getFormulaResult()");
        JSONObject object = new JSONObject();
        JSONObject raw = new JSONObject();
        for(Map.Entry<String, Double> entry : hashMap.entrySet())  {
            raw.put(entry.getKey().toLowerCase(), entry.getValue());
        }
        object.put("rawData", raw.toJSONString());
        Map<String, Object> map = new ConcurrentHashMap<String, Object>();
        map.put("this", object);
        try {
            return FormulaParser.executeFormula(FormulaParser.parse(map, (String) getProperty(FORMULA_DEFINITION)));
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getLocalizedMessage());
            throw new Exception("Error getting Stock result " + e.getLocalizedMessage());
        }
    }

    public static void main(String [] args){
        StockFormulaSensor stockFormulaSensor = new StockFormulaSensor();
        stockFormulaSensor.setProperty(STOCK, "ALU");
        stockFormulaSensor.setProperty(THRESHOLD, 0);
        stockFormulaSensor.setProperty(FORMULA_DEFINITION, "this->price - this->moving_average");
        TestResult testResult = stockFormulaSensor.execute(null);
        log.debug(testResult.getObserverState());
        log.debug(testResult.getRawData());


        stockFormulaSensor.setProperty(STOCK, "GOOG");
        stockFormulaSensor.setProperty(THRESHOLD, .15);
        stockFormulaSensor.setProperty(FORMULA_DEFINITION, "(this->price - this->moving_average)/this->moving_average");
        testResult = stockFormulaSensor.execute(null);
        log.debug(testResult.getObserverState());
        log.debug(testResult.getRawData());

    }
}
