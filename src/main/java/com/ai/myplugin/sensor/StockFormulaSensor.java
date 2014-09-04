package com.ai.myplugin.sensor;

import com.ai.api.*;
import com.ai.myplugin.util.FormulaParser;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by User: veselin
 * On Date: 29/10/13
 */
@PluginImplementation
@PluginHeader(version = "1.0.1", author = "Veselin", category = "Stock", iconURL = "http://app.waylay.io/icons/stock_formula.png")
public class StockFormulaSensor extends StockAbstractSensor {

    private static final Logger log = LoggerFactory.getLogger(StockAbstractSensor.class);

    private static final String RAW_DATA_FORMULA_VALUE = "formulaValue";

    private final FormulaParser formulaParser = new FormulaParser();

    @Override
    public Map<String, PropertyType> getRequiredProperties() {
        Map<String, PropertyType> map = new HashMap<>();
        map.put(STOCK, new PropertyType(DataType.STRING, true, false));
        map.put(THRESHOLD, new PropertyType(DataType.DOUBLE, true, false));
        map.put(FORMULA_DEFINITION, new PropertyType(DataType.STRING, true, false));
        return map;
    }

    @Override
    protected String getSensorName() {
        return "StockFormula";
    }


    @Override
    public void shutdown(SessionContext testSessionContext) {
        log.debug("Shutdown : " + getName() + ", sensor : "+this.getClass().getName());
        formulaParser.resetStats();
    }

    @Override
    public String getDescription() {
        return "Stock exchange sensor, formula computation on the raw data";
    }

    @Override
    public Map<String, RawDataType> getProducedRawData() {
        Map<String, RawDataType> produced = super.getProducedRawData();
        produced.put(RAW_DATA_FORMULA_VALUE, new RawDataType("double", DataType.DOUBLE, true, CollectedType.INSTANT));
        return produced;
    }

    @Override
    protected String getObserverState(Map<String, Double> results, Double threshold) {
        try {
            double res = getFormulaResult(results);
            results.put(RAW_DATA_FORMULA_VALUE, res);
            if(res < threshold) {
                return STATE_BELOW;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return "InvalidResult";
        }
        return STATE_ABOVE;
    }

    private double getFormulaResult(Map<String, Double> hashMap) throws Exception{
        log.debug("getFormulaResult()");
        JSONObject object = new JSONObject();
        JSONObject raw = new JSONObject();
        for(Map.Entry<String, Double> entry : hashMap.entrySet())  {
            raw.put(entry.getKey().toLowerCase(), entry.getValue());
        }
        object.put("rawData", raw.toJSONString());
        Map<String, Object> map = new HashMap<>();
        map.put("this", object);
        try {
            return FormulaParser.executeFormula(formulaParser.parseFormula((String) getProperty(FORMULA_DEFINITION), map));
        } catch (Exception e) {
            log.error(e.getLocalizedMessage(), e);
            throw new Exception("Error getting Stock result " + e.getLocalizedMessage());
        }
    }
}
