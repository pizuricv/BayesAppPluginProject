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
        log.info("formula "+formula);
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
        log.info("formula = " + formula);
        log.info("value = " + value);
        assertEquals(value, 1+3.0);
    }
    public void testCalculationStates() throws ParseException {
        RawFormulaSensor rawFormulaSensor = new RawFormulaSensor();
        String formula = "node1->value1 + node2->value2";
        log.info("formula "+formula);
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
        log.info(testResult.getObserverState());
        log.info(testResult.getRawData());
        assertEquals("Equal", testResult.getObserverState());


        rawFormulaSensor.setProperty("threshold", "3");
        testResult = rawFormulaSensor.execute(testSessionContext);
        log.info(testResult.getObserverState());
        log.info(testResult.getRawData());
        assertEquals("Above", testResult.getObserverState());

        rawFormulaSensor.setProperty("threshold", "5");
        testResult = rawFormulaSensor.execute(testSessionContext);
        log.info(testResult.getObserverState());
        log.info(testResult.getRawData());
        assertEquals("Below", testResult.getObserverState());

        formula =  "node1->value1 / node2->value2 + 3 * ( node1->value1 + node2->value2 )";
        log.info("formula "+formula);
        rawFormulaSensor.setProperty("formula", formula);
        testResult = rawFormulaSensor.execute(testSessionContext);
        log.info(testResult.getObserverState());
        log.info(testResult.getRawData());
        assertEquals("Above", testResult.getObserverState());
        Double value1 = Utils.getDouble(((JSONObject) (new JSONParser().parse(testResult.getRawData()))).get("formulaValue"));

        formula = "node1->value1 / node2->value2 + 3 * (node1->value1 + node2->value2)";
        log.info("formula "+formula);
        rawFormulaSensor.setProperty("formula", formula);
        testResult = rawFormulaSensor.execute(testSessionContext);
        log.info(testResult.getObserverState());
        log.info(testResult.getRawData());
        assertEquals("Above", testResult.getObserverState());
        Double value2 = Utils.getDouble(((JSONObject) (new JSONParser().parse(testResult.getRawData()))).get("formulaValue"));
        assertEquals(value1, value2);
        assertEquals(value1, 12.33, 0.1);


        formula = "node1->value1 - node1->value1";
        log.info("formula "+formula);
        rawFormulaSensor.setProperty("formula", formula);
        rawFormulaSensor.setProperty("threshold", 0);
        testResult = rawFormulaSensor.execute(testSessionContext);
        log.info(testResult.getObserverState());
        log.info(testResult.getRawData());
        assertEquals("Equal", testResult.getObserverState());
        value1 = Utils.getDouble(((JSONObject) (new JSONParser().parse(testResult.getRawData()))).get("formulaValue"));
        assertEquals(value1, 0.);
    }


    public void testCountStringFormula() throws ParseException {
        RawFormulaSensor rawFormulaSensor = new RawFormulaSensor();
        String formula = "node1->value1~Gent";
        log.info("formula "+formula);
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

    public void testDeltaCalculation() throws ParseException {
            RawFormulaSensor rawFormulaSensor = new RawFormulaSensor();
            String formula = "node1->value1 - node1->value1<-1> + node2->value2 - node2->value2<-1>";
            log.info("formula "+formula);
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
            rawFormulaSensor.execute(testSessionContext);

            jsonRaw.put("value1", 2);
            jsonRaw.put("value2", 4);
            jsonObject.put("rawData", jsonRaw.toJSONString());
            mapTestResult.put("node1", jsonObject);
            mapTestResult.put("node2", jsonObject);
            testSessionContext.setAttribute(NodeSessionParams.RAW_DATA, mapTestResult);
            TestResult testResult = rawFormulaSensor.execute(testSessionContext);

            double value = Utils.getDouble(((JSONObject) (new JSONParser().parse(testResult.getRawData()))).get("formulaValue"));
            log.info("formula = " + formula);
            log.info("value = " + value);
            assertEquals(value, 2.0);
    }

    public void testDeltaCalculation2() throws ParseException {
        RawFormulaSensor rawFormulaSensor = new RawFormulaSensor();
        String formula = "node1->value1 - node1->value1<-1> + node2->value2 - node2->value2<-1> - node2->value2<-2>";
        log.info("formula "+formula);
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
        rawFormulaSensor.execute(testSessionContext);

        jsonRaw.put("value1", 2);
        jsonRaw.put("value2", 4);
        jsonObject.put("rawData", jsonRaw.toJSONString());
        mapTestResult.put("node1", jsonObject);
        mapTestResult.put("node2", jsonObject);
        testSessionContext.setAttribute(NodeSessionParams.RAW_DATA, mapTestResult);
        TestResult testResult = rawFormulaSensor.execute(testSessionContext);


        jsonRaw.put("value1", 6);
        jsonRaw.put("value2", 10);
        jsonObject.put("rawData", jsonRaw.toJSONString());
        mapTestResult.put("node1", jsonObject);
        mapTestResult.put("node2", jsonObject);
        testSessionContext.setAttribute(NodeSessionParams.RAW_DATA, mapTestResult);
        testResult = rawFormulaSensor.execute(testSessionContext);

        double value = Utils.getDouble(((JSONObject) (new JSONParser().parse(testResult.getRawData()))).get("formulaValue"));
        log.info("formula = " + formula);
        log.info("value = " + value);
        assertEquals(value, 7.0);
    }
}
