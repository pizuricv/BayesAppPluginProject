package com.ai.myplugin.sensor;

import com.ai.bayes.scenario.TestResult;
import com.ai.myplugin.util.Utils;
import com.ai.util.resource.NodeSessionParams;
import com.ai.util.resource.TestSessionContext;
import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by User: veselin
 * On Date: 25/10/13
 */
public class RawFormulaSensorTest extends TestCase {
    private static final Log log = LogFactory.getLog(RawFormulaSensorTest.class);

    public void testCalculationFormula() throws ParseException {
        RawFormulaSensor rawFormulaSensor = new RawFormulaSensor();
        String formula = "node1->value1 + node2->value2";
        rawFormulaSensor.setProperty("formula", formula);

        rawFormulaSensor.setProperty("threshold", "4");
        TestSessionContext testSessionContext = new TestSessionContext(1);
        Map<String, Object> mapTestResult = new HashMap<String, Object>();
        JSONObject jsonObject = new JSONObject();
        JSONObject jsonRaw = new JSONObject();
        jsonRaw.put("value1", 1);
        jsonRaw.put("value2", 3);
        jsonObject.put("rawData", jsonRaw.toJSONString());
        mapTestResult.put("node1", jsonObject);
        mapTestResult.put("node2", jsonObject);
        testSessionContext.setAttribute(NodeSessionParams.RAW_DATA, mapTestResult);
        TestResult testResult = rawFormulaSensor.execute(testSessionContext);
        double value = Utils.getDouble(((JSONObject) (new JSONParser().parse(testResult.getRawData()))).get("formulaValue"));
        log.debug("formula = " + formula);
        log.debug("value = " + value);
        assertEquals(value, 1+3.0);
    }
    public void testCalculationStates() throws ParseException {
        RawFormulaSensor rawFormulaSensor = new RawFormulaSensor();
        rawFormulaSensor.setProperty("formula", "node1->value1 + node2->value2");

        rawFormulaSensor.setProperty("threshold", "4");
        TestSessionContext testSessionContext = new TestSessionContext(1);
        Map<String, Object> mapTestResult = new HashMap<String, Object>();
        JSONObject jsonObject = new JSONObject();
        JSONObject jsonRaw = new JSONObject();
        jsonRaw.put("value1", 1);
        jsonRaw.put("value2", 3);
        jsonObject.put("rawData", jsonRaw.toJSONString());
        mapTestResult.put("node1", jsonObject);
        mapTestResult.put("node2", jsonObject);
        testSessionContext.setAttribute(NodeSessionParams.RAW_DATA, mapTestResult);
        TestResult testResult = rawFormulaSensor.execute(testSessionContext);
        log.debug(testResult.getObserverState());
        log.debug(testResult.getRawData());
        assertEquals("Equal", testResult.getObserverState());


        rawFormulaSensor.setProperty("threshold", "3");
        testResult = rawFormulaSensor.execute(testSessionContext);
        log.debug(testResult.getObserverState());
        log.debug(testResult.getRawData());
        assertEquals("Above", testResult.getObserverState());

        rawFormulaSensor.setProperty("threshold", "5");
        testResult = rawFormulaSensor.execute(testSessionContext);
        log.debug(testResult.getObserverState());
        log.debug(testResult.getRawData());
        assertEquals("Below", testResult.getObserverState());

        rawFormulaSensor.setProperty("formula", "node1->value1 / node2->value2 + 3 * ( node1->value1 + node2->value2 )");
        testResult = rawFormulaSensor.execute(testSessionContext);
        log.debug(testResult.getObserverState());
        log.debug(testResult.getRawData());
        assertEquals("Above", testResult.getObserverState());
        Double value1 = Utils.getDouble(((JSONObject) (new JSONParser().parse(testResult.getRawData()))).get("formulaValue"));

        rawFormulaSensor.setProperty("formula", "node1->value1 / node2->value2 + 3 * (node1->value1 + node2->value2)");
        testResult = rawFormulaSensor.execute(testSessionContext);
        log.debug(testResult.getObserverState());
        log.debug(testResult.getRawData());
        assertEquals("Above", testResult.getObserverState());
        Double value2 = Utils.getDouble(((JSONObject) (new JSONParser().parse(testResult.getRawData()))).get("formulaValue"));
        assertEquals(value1, value2);
        assertEquals(value1, 12.33, 0.1);


        rawFormulaSensor.setProperty("formula", "node1->value1 - node1->value1");
        rawFormulaSensor.setProperty("threshold", 0);
        testResult = rawFormulaSensor.execute(testSessionContext);
        log.debug(testResult.getObserverState());
        log.debug(testResult.getRawData());
        assertEquals("Equal", testResult.getObserverState());
        value1 = Utils.getDouble(((JSONObject) (new JSONParser().parse(testResult.getRawData()))).get("formulaValue"));
        assertEquals(value1, 0.);
    }


    public void testCountStringFormula() throws ParseException {
        RawFormulaSensor rawFormulaSensor = new RawFormulaSensor();
        String formula = "node1->value1~Gent";
        rawFormulaSensor.setProperty("formula", formula);

        rawFormulaSensor.setProperty("threshold", "1");
        TestSessionContext testSessionContext = new TestSessionContext(1);
        Map<String, Object> mapTestResult = new HashMap<String, Object>();
        JSONObject jsonObject = new JSONObject();
        JSONObject jsonRaw = new JSONObject();
        jsonRaw.put("value1", "GentHelloGent");
        jsonRaw.put("value2", 3);
        jsonObject.put("rawData", jsonRaw.toJSONString());
        mapTestResult.put("node1", jsonObject);
        mapTestResult.put("node2", jsonObject);
        testSessionContext.setAttribute(NodeSessionParams.RAW_DATA, mapTestResult);
        TestResult testResult = rawFormulaSensor.execute(testSessionContext);
        double value = Utils.getDouble(((JSONObject) (new JSONParser().parse(testResult.getRawData()))).get("formulaValue"));
        log.debug("formula = " + formula);
        log.debug("value = " + value);
        assertEquals(value, 2.0);
    }
}
