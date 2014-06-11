package com.ai.myplugin.sensor;

import com.ai.api.DataType;
import com.ai.api.PluginHeader;
import com.ai.api.PropertyType;
import com.ai.api.SessionContext;
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
    protected String getTag() {
        return StockAbstractSensor.FORMULA;
    }

    @Override
    protected String getSensorName() {
        return "StockFormula";
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
            return FormulaParser.executeFormula(formulaParser.parseFormula((String) getProperty(FORMULA_DEFINITION), map));
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getLocalizedMessage());
            throw new Exception("Error getting Stock result " + e.getLocalizedMessage());
        }
    }

    @Override
    public void shutdown(SessionContext testSessionContext) {
        log.debug("Shutdown : " + getName() + ", sensor : "+this.getClass().getName());
        formulaParser.restStats();
    }

    @Override
    public String getDescription() {
        return "Stock exchange sensor, formula computation on the raw data";
    }


}
