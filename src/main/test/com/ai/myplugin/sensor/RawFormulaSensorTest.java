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
        String formula = "<node1.rawData.value1> + <node2.rawData.value2>";
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
        String formula = "<node1.rawData.value1> + <node2.rawData.value2>";
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

        formula =  "<node1.rawData.value1> / <node2.rawData.value2> + 3 * (<node1.rawData.value1> + <node2.rawData.value2> )";
        log.info("formula "+formula);
        rawFormulaSensor.setProperty("formula", formula);
        testResult = rawFormulaSensor.execute(testSessionContext);
        log.info(testResult.getObserverState());
        log.info(testResult.getRawData());
        assertEquals("Above", testResult.getObserverState());
        Double value1 = Utils.getDouble(((JSONObject) (new JSONParser().parse(testResult.getRawData()))).get("formulaValue"));

        formula = "<node1.rawData.value1> / <node2.rawData.value2> + 3 * (<node1.rawData.value1> + <node2.rawData.value2>)";
        log.info("formula "+formula);
        rawFormulaSensor.setProperty("formula", formula);
        testResult = rawFormulaSensor.execute(testSessionContext);
        log.info(testResult.getObserverState());
        log.info(testResult.getRawData());
        assertEquals("Above", testResult.getObserverState());
        Double value2 = Utils.getDouble(((JSONObject) (new JSONParser().parse(testResult.getRawData()))).get("formulaValue"));
        assertEquals(value1, value2);
        assertEquals(value1, 12.33, 0.1);


        formula = "<node1.rawData.value1> - <node1.rawData.value1>";
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

    public void testMultipleStates(){

    }

    public void testMultipleThresholdsAndStates() throws ParseException {
        RawFormulaSensor rawFormulaSensor = new RawFormulaSensor();
        String formula = "<node1.rawData.value1> + <node2.rawData.value2>";
        log.info("formula "+formula);
        rawFormulaSensor.setProperty("formula", formula);

        rawFormulaSensor.setProperty("threshold", "2,5");
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
        assertEquals("level_1", testResult.getObserverState());
        assertEquals(3, rawFormulaSensor.getSupportedStates().length);

    }

    public void testDeltaCalculation() throws ParseException {
            RawFormulaSensor rawFormulaSensor = new RawFormulaSensor();
            String formula = "<node1.rawData.value1> - <node1.rawData.value1>[-1] + <node2.rawData.value2> - <node2.rawData.value2>[-1]";
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
        String formula = "<node1.rawData.value1> - <node1.rawData.value1>[-1] + <node2.rawData.value2> - " +
                "<node2.rawData.value2>[-1] - <node2.rawData.value2>[-2]";
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

    public void testDeltaTimeCalculation2() throws ParseException {
        RawFormulaSensor rawFormulaSensor = new RawFormulaSensor();
        String formula = "abs( <node1.rawData.value1> - <node1.rawData.value1>[-1] + <node2.rawData.value2> - " +
                "<node2.rawData.value2>[-1] - <node2.rawData.value2>[-1] ) / dt";
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
        rawFormulaSensor.execute(testSessionContext);


        jsonRaw.put("value1", 6);
        jsonRaw.put("value2", 10);
        jsonObject.put("rawData", jsonRaw.toJSONString());
        mapTestResult.put("node1", jsonObject);
        mapTestResult.put("node2", jsonObject);
        testSessionContext.setAttribute(NodeSessionParams.RAW_DATA, mapTestResult);



        try{
           rawFormulaSensor.execute(testSessionContext);
        } catch (ArithmeticException e) {
            if(!e.getMessage().contains("zero"))
                fail();
        }
    }

    public void testStatsCalculation() throws ParseException {
        RawFormulaSensor rawFormulaSensor = new RawFormulaSensor();
        String formula = "<avg(node1.rawData.value1)> - <min(node1.rawData.value1)> + <max(node2.rawData.value2)> - " +
                "<node2.rawData.value2>[-1] - <node2.rawData.value2>[-2]";
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
        assertEquals(3.0 - 1.0 + 10.0 - 4.0 - 3.0, value);
    }

    public void testStatsCalculationWithSampleSize() throws ParseException {
        RawFormulaSensor rawFormulaSensor = new RawFormulaSensor();
        String formula = "<avg(3, samples, node3.rawData.value1)> - <min(3, samples, node3.rawData.value1)> + <max(3, samples, node4.rawData.value2)> - " +
                "<node4.rawData.value2>[-1] - <node4.rawData.value2>[-2]";
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
        mapTestResult.put("node3", jsonObject);
        mapTestResult.put("node4", jsonObject);
        testSessionContext.setAttribute(NodeSessionParams.RAW_DATA, mapTestResult);
        rawFormulaSensor.execute(testSessionContext);

        jsonRaw.put("value1", 2);
        jsonRaw.put("value2", 4);
        jsonObject.put("rawData", jsonRaw.toJSONString());
        mapTestResult.put("node3", jsonObject);
        mapTestResult.put("node4", jsonObject);
        testSessionContext.setAttribute(NodeSessionParams.RAW_DATA, mapTestResult);
        TestResult testResult = rawFormulaSensor.execute(testSessionContext);


        jsonRaw.put("value1", 6);
        jsonRaw.put("value2", 10);
        jsonObject.put("rawData", jsonRaw.toJSONString());
        mapTestResult.put("node3", jsonObject);
        mapTestResult.put("node4", jsonObject);
        testSessionContext.setAttribute(NodeSessionParams.RAW_DATA, mapTestResult);
        testResult = rawFormulaSensor.execute(testSessionContext);

        double value = Utils.getDouble(((JSONObject) (new JSONParser().parse(testResult.getRawData()))).get("formulaValue"));
        log.info("formula = " + formula);
        log.info("value = " + value);
        assertEquals(3.0 - 1.0 + 10.0 - 4.0 - 3.0, value);
    }

    public void testStatsCalculationWithSampleSizeWithSmallerBuffer() throws ParseException {
        RawFormulaSensor rawFormulaSensor = new RawFormulaSensor();
        String formula = "<avg(2, samples, node5.rawData.value1)> - <min(2, samples, node5.rawData.value1)> + <max(2, samples, node6.rawData.value2)> - " +
                "<node6.rawData.value2>[-1] - <node6.rawData.value2>[-2]";
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
        mapTestResult.put("node5", jsonObject);
        mapTestResult.put("node6", jsonObject);
        testSessionContext.setAttribute(NodeSessionParams.RAW_DATA, mapTestResult);
        rawFormulaSensor.execute(testSessionContext);

        jsonRaw.put("value1", 2);
        jsonRaw.put("value2", 4);
        jsonObject.put("rawData", jsonRaw.toJSONString());
        mapTestResult.put("node5", jsonObject);
        mapTestResult.put("node6", jsonObject);
        testSessionContext.setAttribute(NodeSessionParams.RAW_DATA, mapTestResult);
        TestResult testResult = rawFormulaSensor.execute(testSessionContext);


        jsonRaw.put("value1", 6);
        jsonRaw.put("value2", 10);
        jsonObject.put("rawData", jsonRaw.toJSONString());
        mapTestResult.put("node5", jsonObject);
        mapTestResult.put("node6", jsonObject);
        testSessionContext.setAttribute(NodeSessionParams.RAW_DATA, mapTestResult);
        testResult = rawFormulaSensor.execute(testSessionContext);

        double value = Utils.getDouble(((JSONObject) (new JSONParser().parse(testResult.getRawData()))).get("formulaValue"));
        log.info("formula = " + formula);
        log.info("value = " + value);
        assertEquals(4.0 - 2.0 + 10.0 - 4.0 - 3.0, value);
    }


    public void testStatsCalculationWithSlidingWindow() throws ParseException {
        RawFormulaSensor rawFormulaSensor = new RawFormulaSensor();
        String formula = "<avg(3, min, node7.rawData.value1)> - <min(3, min, node7.rawData.value1)> + <max(3, min, node8.rawData.value2)> - " +
                "<node8.rawData.value2>[-1] - <node8.rawData.value2>[-2]";
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
        jsonObject.put("time", System.currentTimeMillis()/1000);
        mapTestResult.put("node7", jsonObject);
        mapTestResult.put("node8", jsonObject);
        testSessionContext.setAttribute(NodeSessionParams.RAW_DATA, mapTestResult);
        rawFormulaSensor.execute(testSessionContext);

        jsonRaw.put("value1", 2);
        jsonRaw.put("value2", 4);
        jsonObject.put("rawData", jsonRaw.toJSONString());
        mapTestResult.put("node7", jsonObject);
        mapTestResult.put("node8", jsonObject);
        testSessionContext.setAttribute(NodeSessionParams.RAW_DATA, mapTestResult);
        TestResult testResult = rawFormulaSensor.execute(testSessionContext);


        jsonRaw.put("value1", 6);
        jsonRaw.put("value2", 10);
        jsonObject.put("rawData", jsonRaw.toJSONString());
        mapTestResult.put("node7", jsonObject);
        mapTestResult.put("node8", jsonObject);
        testSessionContext.setAttribute(NodeSessionParams.RAW_DATA, mapTestResult);
        testResult = rawFormulaSensor.execute(testSessionContext);

        double value = Utils.getDouble(((JSONObject) (new JSONParser().parse(testResult.getRawData()))).get("formulaValue"));
        log.info("formula = " + formula);
        log.info("value = " + value);
        assertEquals(3.0 - 1.0 + 10.0 - 4.0 - 3.0, value);
    }
}
