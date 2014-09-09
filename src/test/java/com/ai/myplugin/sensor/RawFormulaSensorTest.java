/**
 * Created by User: veselin
 * On Date: 25/10/13
 */

package com.ai.myplugin.sensor;

import com.ai.api.SensorResult;
import com.ai.api.SessionContext;
import com.ai.api.SessionParams;
import com.ai.myplugin.util.Utils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.*;

public class RawFormulaSensorTest {

    @Test
    public void testCalculationFormula() throws ParseException {
        RawFormulaSensor rawFormulaSensor = new RawFormulaSensor();
        String formula = "<node1.rawData.value1> + <node2.rawData.value2>";
        System.out.println("formula "+formula);
        rawFormulaSensor.setProperty("formula", formula);

        rawFormulaSensor.setProperty("threshold", "4");
        SessionContext SessionContext = new SessionContext(1);
        Map<String, Object> mapSensorResult = new HashMap<>();
        JSONObject jsonObject = new JSONObject();
        JSONObject jsonRaw = new JSONObject();
        jsonRaw.put("value1", 1);
        jsonRaw.put("value2", 3);
        jsonObject.put("rawData", jsonRaw.toJSONString());
        mapSensorResult.put("node1", jsonObject);
        mapSensorResult.put("node2", jsonObject);
        SessionContext.setAttribute(SessionParams.RAW_DATA, mapSensorResult);
        SensorResult SensorResult = rawFormulaSensor.execute(SessionContext);
        double value = Utils.getDouble(((JSONObject) (new JSONParser().parse(SensorResult.getRawData()))).get("formulaValue"));
        System.out.println("formula = " + formula);
        System.out.println("value = " + value);
        assertEquals(value, 1+3.0, 0.0001);
    }

    @Test
    public void testCalculationStates() throws ParseException {
        RawFormulaSensor rawFormulaSensor = new RawFormulaSensor();
        String formula = "<node1.rawData.value1> + <node2.rawData.value2>";
        System.out.println("formula "+formula);
        rawFormulaSensor.setProperty("formula", formula);

        rawFormulaSensor.setProperty("threshold", "4");
        SessionContext SessionContext = new SessionContext(1);
        Map<String, Object> mapSensorResult = new HashMap<String, Object>();
        JSONObject jsonObject = new JSONObject();
        JSONObject jsonRaw = new JSONObject();
        jsonRaw.put("value1", 1);
        jsonRaw.put("value2", 3);
        jsonObject.put("rawData", jsonRaw.toJSONString());
        mapSensorResult.put("node1", jsonObject);
        mapSensorResult.put("node2", jsonObject);
        SessionContext.setAttribute(SessionParams.RAW_DATA, mapSensorResult);
        SensorResult SensorResult = rawFormulaSensor.execute(SessionContext);
        System.out.println(SensorResult.getObserverState());
        System.out.println(SensorResult.getRawData());
        assertEquals("Equal", SensorResult.getObserverState());


        rawFormulaSensor.setProperty("threshold", "3");
        SensorResult = rawFormulaSensor.execute(SessionContext);
        System.out.println(SensorResult.getObserverState());
        System.out.println(SensorResult.getRawData());
        assertEquals("Above", SensorResult.getObserverState());

        rawFormulaSensor.setProperty("threshold", "5");
        SensorResult = rawFormulaSensor.execute(SessionContext);
        System.out.println(SensorResult.getObserverState());
        System.out.println(SensorResult.getRawData());
        assertEquals("Below", SensorResult.getObserverState());

        formula =  "<node1.rawData.value1> / <node2.rawData.value2> + 3 * (<node1.rawData.value1> + <node2.rawData.value2> )";
        System.out.println("formula "+formula);
        rawFormulaSensor.setProperty("formula", formula);
        SensorResult = rawFormulaSensor.execute(SessionContext);
        System.out.println(SensorResult.getObserverState());
        System.out.println(SensorResult.getRawData());
        assertEquals("Above", SensorResult.getObserverState());
        Double value1 = Utils.getDouble(((JSONObject) (new JSONParser().parse(SensorResult.getRawData()))).get("formulaValue"));

        formula = "<node1.rawData.value1> / <node2.rawData.value2> + 3 * (<node1.rawData.value1> + <node2.rawData.value2>)";
        System.out.println("formula "+formula);
        rawFormulaSensor.setProperty("formula", formula);
        SensorResult = rawFormulaSensor.execute(SessionContext);
        System.out.println(SensorResult.getObserverState());
        System.out.println(SensorResult.getRawData());
        assertEquals("Above", SensorResult.getObserverState());
        Double value2 = Utils.getDouble(((JSONObject) (new JSONParser().parse(SensorResult.getRawData()))).get("formulaValue"));
        assertEquals(value1, value2);
        assertEquals(value1, 12.33, 0.1);


        formula = "<node1.rawData.value1> - <node1.rawData.value1>";
        System.out.println("formula "+formula);
        rawFormulaSensor.setProperty("formula", formula);
        rawFormulaSensor.setProperty("threshold", 0);
        SensorResult = rawFormulaSensor.execute(SessionContext);
        System.out.println(SensorResult.getObserverState());
        System.out.println(SensorResult.getRawData());
        assertEquals("Equal", SensorResult.getObserverState());
        value1 = Utils.getDouble(((JSONObject) (new JSONParser().parse(SensorResult.getRawData()))).get("formulaValue"));
        assertEquals(value1, Double.valueOf(0.));
    }

    @Test
    public void testMultipleStates(){

    }

    @Test
    public void testMultipleThresholdsAndStates() throws ParseException {
        RawFormulaSensor rawFormulaSensor = new RawFormulaSensor();
        String formula = "<node1.rawData.value1> + <node2.rawData.value2>";
        System.out.println("formula "+formula);
        rawFormulaSensor.setProperty("formula", formula);

        rawFormulaSensor.setProperty("threshold", "2,5");
        SessionContext SessionContext = new SessionContext(1);
        Map<String, Object> mapSensorResult = new HashMap<String, Object>();
        JSONObject jsonObject = new JSONObject();
        JSONObject jsonRaw = new JSONObject();
        jsonRaw.put("value1", 1);
        jsonRaw.put("value2", 3);
        jsonObject.put("rawData", jsonRaw.toJSONString());
        mapSensorResult.put("node1", jsonObject);
        mapSensorResult.put("node2", jsonObject);
        SessionContext.setAttribute(SessionParams.RAW_DATA, mapSensorResult);
        SensorResult SensorResult = rawFormulaSensor.execute(SessionContext);
        System.out.println(SensorResult.getObserverState());
        System.out.println(SensorResult.getRawData());
        assertEquals("level_1", SensorResult.getObserverState());
        assertEquals(3, rawFormulaSensor.getSupportedStates().size());

    }

    @Test
    public void testDeltaCalculation() throws ParseException {
            RawFormulaSensor rawFormulaSensor = new RawFormulaSensor();
            String formula = "<node1.rawData.value1> - <node1.rawData.value1>[-1] + <node2.rawData.value2> - <node2.rawData.value2>[-1]";
            System.out.println("formula "+formula);
            rawFormulaSensor.setProperty("formula", formula);

            rawFormulaSensor.setProperty("threshold", "4");
            SessionContext SessionContext = new SessionContext(1);
            Map<String, Object> mapSensorResult = new HashMap<String, Object>();
            JSONObject jsonObject = new JSONObject();
            JSONObject jsonRaw = new JSONObject();
            jsonRaw.put("value1", 1);
            jsonRaw.put("value2", 3);
            jsonObject.put("rawData", jsonRaw.toJSONString());
            mapSensorResult.put("node1", jsonObject);
            mapSensorResult.put("node2", jsonObject);
            SessionContext.setAttribute(SessionParams.RAW_DATA, mapSensorResult);
            rawFormulaSensor.execute(SessionContext);

            jsonRaw.put("value1", 2);
            jsonRaw.put("value2", 4);
            jsonObject.put("rawData", jsonRaw.toJSONString());
            mapSensorResult.put("node1", jsonObject);
            mapSensorResult.put("node2", jsonObject);
            SessionContext.setAttribute(SessionParams.RAW_DATA, mapSensorResult);
            SensorResult SensorResult = rawFormulaSensor.execute(SessionContext);

            double value = Utils.getDouble(((JSONObject) (new JSONParser().parse(SensorResult.getRawData()))).get("formulaValue"));
            System.out.println("formula = " + formula);
            System.out.println("value = " + value);
            assertEquals(value, 2.0, 0.0001);
    }

    @Test
    public void testDeltaCalculation2() throws ParseException {
        RawFormulaSensor rawFormulaSensor = new RawFormulaSensor();
        String formula = "<node1.rawData.value1> - <node1.rawData.value1>[-1] + <node2.rawData.value2> - " +
                "<node2.rawData.value2>[-1] - <node2.rawData.value2>[-2]";
        System.out.println("formula "+formula);
        rawFormulaSensor.setProperty("formula", formula);

        rawFormulaSensor.setProperty("threshold", "4");
        SessionContext SessionContext = new SessionContext(1);
        Map<String, Object> mapSensorResult = new HashMap<String, Object>();
        JSONObject jsonObject = new JSONObject();
        JSONObject jsonRaw = new JSONObject();
        jsonRaw.put("value1", 1);
        jsonRaw.put("value2", 3);
        jsonObject.put("rawData", jsonRaw.toJSONString());
        mapSensorResult.put("node1", jsonObject);
        mapSensorResult.put("node2", jsonObject);
        SessionContext.setAttribute(SessionParams.RAW_DATA, mapSensorResult);
        rawFormulaSensor.execute(SessionContext);

        jsonRaw.put("value1", 2);
        jsonRaw.put("value2", 4);
        jsonObject.put("rawData", jsonRaw.toJSONString());
        mapSensorResult.put("node1", jsonObject);
        mapSensorResult.put("node2", jsonObject);
        SessionContext.setAttribute(SessionParams.RAW_DATA, mapSensorResult);
        SensorResult SensorResult = rawFormulaSensor.execute(SessionContext);


        jsonRaw.put("value1", 6);
        jsonRaw.put("value2", 10);
        jsonObject.put("rawData", jsonRaw.toJSONString());
        mapSensorResult.put("node1", jsonObject);
        mapSensorResult.put("node2", jsonObject);
        SessionContext.setAttribute(SessionParams.RAW_DATA, mapSensorResult);
        SensorResult = rawFormulaSensor.execute(SessionContext);

        double value = Utils.getDouble(((JSONObject) (new JSONParser().parse(SensorResult.getRawData()))).get("formulaValue"));
        System.out.println("formula = " + formula);
        System.out.println("value = " + value);
        assertEquals(value, 7.0, 0.0001);
    }

    public void testDeltaTimeCalculation2() throws ParseException {
        RawFormulaSensor rawFormulaSensor = new RawFormulaSensor();
        String formula = "abs( <node1.rawData.value1> - <node1.rawData.value1>[-1] + <node2.rawData.value2> - " +
                "<node2.rawData.value2>[-1] - <node2.rawData.value2>[-1] ) / dt";
        System.out.println("formula "+formula);
        rawFormulaSensor.setProperty("formula", formula);

        rawFormulaSensor.setProperty("threshold", "4");
        SessionContext SessionContext = new SessionContext(1);
        Map<String, Object> mapSensorResult = new HashMap<String, Object>();
        JSONObject jsonObject = new JSONObject();
        JSONObject jsonRaw = new JSONObject();
        jsonRaw.put("value1", 1);
        jsonRaw.put("value2", 3);
        jsonObject.put("rawData", jsonRaw.toJSONString());
        mapSensorResult.put("node1", jsonObject);
        mapSensorResult.put("node2", jsonObject);
        SessionContext.setAttribute(SessionParams.RAW_DATA, mapSensorResult);
        rawFormulaSensor.execute(SessionContext);

        jsonRaw.put("value1", 2);
        jsonRaw.put("value2", 4);
        jsonObject.put("rawData", jsonRaw.toJSONString());
        mapSensorResult.put("node1", jsonObject);
        mapSensorResult.put("node2", jsonObject);
        SessionContext.setAttribute(SessionParams.RAW_DATA, mapSensorResult);
        rawFormulaSensor.execute(SessionContext);


        jsonRaw.put("value1", 6);
        jsonRaw.put("value2", 10);
        jsonObject.put("rawData", jsonRaw.toJSONString());
        mapSensorResult.put("node1", jsonObject);
        mapSensorResult.put("node2", jsonObject);
        SessionContext.setAttribute(SessionParams.RAW_DATA, mapSensorResult);



        try{
           rawFormulaSensor.execute(SessionContext);
        } catch (ArithmeticException e) {
            if(!e.getMessage().contains("zero"))
                fail();
        }
    }

    @Test
    public void testStatsCalculation() throws ParseException {
        RawFormulaSensor rawFormulaSensor = new RawFormulaSensor();
        String formula = "<avg(node1.rawData.value1)> - <min(node1.rawData.value1)> + <max(node2.rawData.value2)> - " +
                "<node2.rawData.value2>[-1] - <node2.rawData.value2>[-2]";
        System.out.println("formula "+formula);
        rawFormulaSensor.setProperty("formula", formula);

        rawFormulaSensor.setProperty("threshold", "4");
        SessionContext SessionContext = new SessionContext(1);
        Map<String, Object> mapSensorResult = new HashMap<String, Object>();
        JSONObject jsonObject = new JSONObject();
        JSONObject jsonRaw = new JSONObject();
        jsonRaw.put("value1", 1);
        jsonRaw.put("value2", 3);
        jsonObject.put("rawData", jsonRaw.toJSONString());
        mapSensorResult.put("node1", jsonObject);
        mapSensorResult.put("node2", jsonObject);
        SessionContext.setAttribute(SessionParams.RAW_DATA, mapSensorResult);
        rawFormulaSensor.execute(SessionContext);

        jsonRaw.put("value1", 2);
        jsonRaw.put("value2", 4);
        jsonObject.put("rawData", jsonRaw.toJSONString());
        mapSensorResult.put("node1", jsonObject);
        mapSensorResult.put("node2", jsonObject);
        SessionContext.setAttribute(SessionParams.RAW_DATA, mapSensorResult);
        SensorResult SensorResult = rawFormulaSensor.execute(SessionContext);


        jsonRaw.put("value1", 6);
        jsonRaw.put("value2", 10);
        jsonObject.put("rawData", jsonRaw.toJSONString());
        mapSensorResult.put("node1", jsonObject);
        mapSensorResult.put("node2", jsonObject);
        SessionContext.setAttribute(SessionParams.RAW_DATA, mapSensorResult);
        SensorResult = rawFormulaSensor.execute(SessionContext);

        double value = Utils.getDouble(((JSONObject) (new JSONParser().parse(SensorResult.getRawData()))).get("formulaValue"));
        System.out.println("formula = " + formula);
        System.out.println("value = " + value);
        assertEquals(3.0 - 1.0 + 10.0 - 4.0 - 3.0, value, 0.0001);
    }

    @Test
    public void testStatsCalculationWithSampleSize() throws ParseException {
        RawFormulaSensor rawFormulaSensor = new RawFormulaSensor();
        String formula = "<avg(3, samples, node3.rawData.value1)> - <min(3, samples, node3.rawData.value1)> + <max(3, samples, node4.rawData.value2)> - " +
                "<node4.rawData.value2>[-1] - <node4.rawData.value2>[-2]";
        System.out.println("formula "+formula);
        rawFormulaSensor.setProperty("formula", formula);

        rawFormulaSensor.setProperty("threshold", "4");
        SessionContext SessionContext = new SessionContext(1);
        Map<String, Object> mapSensorResult = new HashMap<String, Object>();
        JSONObject jsonObject = new JSONObject();
        JSONObject jsonRaw = new JSONObject();
        jsonRaw.put("value1", 1);
        jsonRaw.put("value2", 3);
        jsonObject.put("rawData", jsonRaw.toJSONString());
        mapSensorResult.put("node3", jsonObject);
        mapSensorResult.put("node4", jsonObject);
        SessionContext.setAttribute(SessionParams.RAW_DATA, mapSensorResult);
        rawFormulaSensor.execute(SessionContext);

        jsonRaw.put("value1", 2);
        jsonRaw.put("value2", 4);
        jsonObject.put("rawData", jsonRaw.toJSONString());
        mapSensorResult.put("node3", jsonObject);
        mapSensorResult.put("node4", jsonObject);
        SessionContext.setAttribute(SessionParams.RAW_DATA, mapSensorResult);
        SensorResult SensorResult = rawFormulaSensor.execute(SessionContext);


        jsonRaw.put("value1", 6);
        jsonRaw.put("value2", 10);
        jsonObject.put("rawData", jsonRaw.toJSONString());
        mapSensorResult.put("node3", jsonObject);
        mapSensorResult.put("node4", jsonObject);
        SessionContext.setAttribute(SessionParams.RAW_DATA, mapSensorResult);
        SensorResult = rawFormulaSensor.execute(SessionContext);

        double value = Utils.getDouble(((JSONObject) (new JSONParser().parse(SensorResult.getRawData()))).get("formulaValue"));
        System.out.println("formula = " + formula);
        System.out.println("value = " + value);
        assertEquals(3.0 - 1.0 + 10.0 - 4.0 - 3.0, value, 0.0001);
    }

    @Test
    public void testStatsCalculationWithSampleSizeWithSmallerBuffer() throws ParseException {
        RawFormulaSensor rawFormulaSensor = new RawFormulaSensor();
        String formula = "<avg(2, samples, node5.rawData.value1)> - <min(2, samples, node5.rawData.value1)> + <max(2, samples, node6.rawData.value2)> - " +
                "<node6.rawData.value2>[-1] - <node6.rawData.value2>[-2]";
        System.out.println("formula "+formula);
        rawFormulaSensor.setProperty("formula", formula);

        rawFormulaSensor.setProperty("threshold", "4");
        SessionContext SessionContext = new SessionContext(1);
        Map<String, Object> mapSensorResult = new HashMap<String, Object>();
        JSONObject jsonObject = new JSONObject();
        JSONObject jsonRaw = new JSONObject();
        jsonRaw.put("value1", 1);
        jsonRaw.put("value2", 3);
        jsonObject.put("rawData", jsonRaw.toJSONString());
        mapSensorResult.put("node5", jsonObject);
        mapSensorResult.put("node6", jsonObject);
        SessionContext.setAttribute(SessionParams.RAW_DATA, mapSensorResult);
        rawFormulaSensor.execute(SessionContext);

        jsonRaw.put("value1", 2);
        jsonRaw.put("value2", 4);
        jsonObject.put("rawData", jsonRaw.toJSONString());
        mapSensorResult.put("node5", jsonObject);
        mapSensorResult.put("node6", jsonObject);
        SessionContext.setAttribute(SessionParams.RAW_DATA, mapSensorResult);
        SensorResult SensorResult = rawFormulaSensor.execute(SessionContext);


        jsonRaw.put("value1", 6);
        jsonRaw.put("value2", 10);
        jsonObject.put("rawData", jsonRaw.toJSONString());
        mapSensorResult.put("node5", jsonObject);
        mapSensorResult.put("node6", jsonObject);
        SessionContext.setAttribute(SessionParams.RAW_DATA, mapSensorResult);
        SensorResult = rawFormulaSensor.execute(SessionContext);

        double value = Utils.getDouble(((JSONObject) (new JSONParser().parse(SensorResult.getRawData()))).get("formulaValue"));
        System.out.println("formula = " + formula);
        System.out.println("value = " + value);
        assertEquals(4.0 - 2.0 + 10.0 - 4.0 - 3.0, value, 0.0001);
    }

    @Test
    public void testStatsCalculationWithSlidingWindow() throws ParseException {
        RawFormulaSensor rawFormulaSensor = new RawFormulaSensor();
        String formula = "<avg(3, min, node7.rawData.value1)> - <min(3, min, node7.rawData.value1)> + <max(3, min, node8.rawData.value2)> - " +
                "<node8.rawData.value2>[-1] - <node8.rawData.value2>[-2]";
        System.out.println("formula "+formula);
        rawFormulaSensor.setProperty("formula", formula);

        rawFormulaSensor.setProperty("threshold", "4");
        SessionContext SessionContext = new SessionContext(1);
        Map<String, Object> mapSensorResult = new HashMap<String, Object>();
        JSONObject jsonObject = new JSONObject();
        JSONObject jsonRaw = new JSONObject();
        jsonRaw.put("value1", 1);
        jsonRaw.put("value2", 3);
        jsonObject.put("rawData", jsonRaw.toJSONString());
        jsonObject.put("time", System.currentTimeMillis()/1000);
        mapSensorResult.put("node7", jsonObject);
        mapSensorResult.put("node8", jsonObject);
        SessionContext.setAttribute(SessionParams.RAW_DATA, mapSensorResult);
        rawFormulaSensor.execute(SessionContext);

        jsonRaw.put("value1", 2);
        jsonRaw.put("value2", 4);
        jsonObject.put("rawData", jsonRaw.toJSONString());
        mapSensorResult.put("node7", jsonObject);
        mapSensorResult.put("node8", jsonObject);
        SessionContext.setAttribute(SessionParams.RAW_DATA, mapSensorResult);
        SensorResult SensorResult = rawFormulaSensor.execute(SessionContext);


        jsonRaw.put("value1", 6);
        jsonRaw.put("value2", 10);
        jsonObject.put("rawData", jsonRaw.toJSONString());
        mapSensorResult.put("node7", jsonObject);
        mapSensorResult.put("node8", jsonObject);
        SessionContext.setAttribute(SessionParams.RAW_DATA, mapSensorResult);
        SensorResult = rawFormulaSensor.execute(SessionContext);

        double value = Utils.getDouble(((JSONObject) (new JSONParser().parse(SensorResult.getRawData()))).get("formulaValue"));
        System.out.println("formula = " + formula);
        System.out.println("value = " + value);
        assertEquals(3.0 - 1.0 + 10.0 - 4.0 - 3.0, value, 0.0001);
    }

    @Test
    public void testStatsCalculationWithJSONArray() throws ParseException {
        RawFormulaSensor rawFormulaSensor = new RawFormulaSensor();
        String formula = "<avg(node9.rawData.value1)> - <min(node10.rawData.value1)> + <max(node9.rawData.value2)>";
        System.out.println("formula "+formula);
        rawFormulaSensor.setProperty("formula", formula);

        rawFormulaSensor.setProperty("threshold", "4");
        SessionContext SessionContext = new SessionContext(1);
        Map<String, Object> mapSensorResult = new HashMap<String, Object>();
        JSONObject jsonObject = new JSONObject();
        JSONObject jsonRaw = new JSONObject();
        JSONArray jsonArray1 = new JSONArray();
        for(int i = 0; i<3 ; i++){
            jsonArray1.add(new Long(i));
        }

        jsonRaw.put("value1", jsonArray1);
        jsonRaw.put("value2", jsonArray1);
        jsonObject.put("rawData", jsonRaw.toJSONString());
        jsonObject.put("time", System.currentTimeMillis()/1000);
        mapSensorResult.put("node9", jsonObject);
        mapSensorResult.put("node10", jsonObject);
        SessionContext.setAttribute(SessionParams.RAW_DATA, mapSensorResult);
        SensorResult SensorResult = rawFormulaSensor.execute(SessionContext);
        double value = Utils.getDouble(((JSONObject) (new JSONParser().parse(SensorResult.getRawData()))).get("formulaValue"));
        System.out.println("formula = " + formula);
        System.out.println("value = " + value);
        assertEquals(1. - 0 + 2, value, 0.0001);
    }

    @Test
    public void test() throws ParseException {

        Long time = System.currentTimeMillis()/1000;

        RawFormulaSensor rawFormulaSensor = new RawFormulaSensor();
        rawFormulaSensor.setProperty("formula", "<node1.value1> + <node2.value2>");

        rawFormulaSensor.setProperty("threshold", "4");
        SessionContext testSessionContext = new SessionContext(1);
        Map mapTestResult = new HashMap<>();
        JSONObject objRaw = new JSONObject();
        objRaw.put("value1", 1);
        objRaw.put("time", time);
        mapTestResult.put("node1", objRaw);

        objRaw = new JSONObject();
        objRaw.put("value2", 1);
        objRaw.put("time", time);
        mapTestResult.put("node2", objRaw);

        testSessionContext.setAttribute(SessionParams.RAW_DATA, mapTestResult);
        SensorResult testResult = rawFormulaSensor.execute(testSessionContext);
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
        mapTestResult = new ConcurrentHashMap<>();
        mapTestResult.put("GOOG", new JSONParser().parse(testResult.getRawData()));
        testSessionContext.setAttribute(SessionParams.RAW_DATA, mapTestResult);

        rawFormulaSensor.setProperty("formula", "<GOOG.price> - <GOOG.moving_average>");
        rawFormulaSensor.setProperty("threshold", 100);
        testResult = rawFormulaSensor.execute(testSessionContext);
        System.out.println(testResult.getObserverState());
        System.out.println(testResult.getRawData());


        rawFormulaSensor.setProperty("formula", "<GOOG.price>");
        rawFormulaSensor.setProperty("threshold", 100);
        testResult = rawFormulaSensor.execute(testSessionContext);
        System.out.println(testResult.getObserverState());
        System.out.println(testResult.getRawData());
    }

    public void testCountFormula() throws ParseException {
        RawFormulaSensor rawFormulaSensor = new RawFormulaSensor();
        String formula = "<count(hello, node1.rawData.value1)> + <node2.rawData.value2>";
        System.out.println("formula "+formula);
        rawFormulaSensor.setProperty("formula", formula);

        rawFormulaSensor.setProperty("threshold", "4");
        SessionContext SessionContext = new SessionContext(1);
        Map mapSensorResult = new HashMap<>();
        JSONObject jsonObject = new JSONObject();
        JSONObject jsonRaw = new JSONObject();
        jsonRaw.put("value1", "hello hello world");
        jsonRaw.put("value2", 3);
        jsonObject.put("rawData", jsonRaw.toJSONString());
        mapSensorResult.put("node1", jsonObject);
        mapSensorResult.put("node2", jsonObject);
        SessionContext.setAttribute(SessionParams.RAW_DATA, mapSensorResult);
        SensorResult SensorResult = rawFormulaSensor.execute(SessionContext);
        double value = Utils.getDouble(((JSONObject) (new JSONParser().parse(SensorResult.getRawData()))).get("formulaValue"));
        System.out.println("formula = " + formula);
        System.out.println("value = " + value);
        assertEquals(value, 2+3.0, 0.0001);
    }

    public void testCountStateFormula() throws ParseException {
        RawFormulaSensor rawFormulaSensor = new RawFormulaSensor();
        String formula = "<count(OK, node1.rawData.state)>";
        System.out.println("formula "+formula);
        rawFormulaSensor.setProperty("formula", formula);

        rawFormulaSensor.setProperty("threshold", "4");
        SessionContext SessionContext = new SessionContext(1);
        Map<String, Object> mapSensorResult = new HashMap<>();
        JSONObject jsonObject = new JSONObject();
        JSONObject jsonRaw = new JSONObject();
        jsonRaw.put("state", "OK");
        jsonObject.put("rawData", jsonRaw.toJSONString());
        mapSensorResult.put("node1", jsonObject);
        SessionContext.setAttribute(SessionParams.RAW_DATA, mapSensorResult);
        SensorResult SensorResult = rawFormulaSensor.execute(SessionContext);
        double value = Utils.getDouble(((JSONObject) (new JSONParser().parse(SensorResult.getRawData()))).get("formulaValue"));
        System.out.println("formula = " + formula);
        System.out.println("value = " + value);
        assertEquals(value, 1.0, 0.0001);
    }

    public void testStateChangeFormula() throws ParseException {
        RawFormulaSensor rawFormulaSensor = new RawFormulaSensor();
        String formula = "<sequence([OK,NOK], node1.rawData.state)>";
        System.out.println("formula "+formula);
        rawFormulaSensor.setProperty("formula", formula);

        rawFormulaSensor.setProperty("threshold", "0");
        SessionContext SessionContext = new SessionContext(1);
        Map<String, Object> mapSensorResult = new HashMap<>();
        JSONObject jsonObject = new JSONObject();
        JSONObject jsonRaw = new JSONObject();
        jsonRaw.put("state", "OK");
        jsonObject.put("rawData", jsonRaw.toJSONString());
        mapSensorResult.put("node1", jsonObject);
        SessionContext.setAttribute(SessionParams.RAW_DATA, mapSensorResult);
        System.out.print(rawFormulaSensor.execute(SessionContext).isSuccess());

        jsonRaw.put("state", "NOK");
        jsonObject.put("rawData", jsonRaw.toJSONString());
        mapSensorResult.put("node1", jsonObject);
        SessionContext.setAttribute(SessionParams.RAW_DATA, mapSensorResult);
        SensorResult SensorResult = rawFormulaSensor.execute(SessionContext);
        System.out.print(SensorResult.getRawData());
        double value = Utils.getDouble(((JSONObject) (new JSONParser().parse(SensorResult.getRawData()))).get("formulaValue"));
        System.out.println("formula = " + formula);
        System.out.println("value = " + value);
        assertEquals(value, 1.0, 0.0001);

        jsonRaw.put("state", "NOK");
        jsonObject.put("rawData", jsonRaw.toJSONString());
        mapSensorResult.put("node1", jsonObject);
        SessionContext.setAttribute(SessionParams.RAW_DATA, mapSensorResult);
        SensorResult = rawFormulaSensor.execute(SessionContext);
        System.out.print(SensorResult.getRawData());
        value = Utils.getDouble(((JSONObject) (new JSONParser().parse(SensorResult.getRawData()))).get("formulaValue"));
        System.out.println("formula = " + formula);
        System.out.println("value = " + value);
        assertEquals(value, 0.0, 0.0001);

        jsonRaw.put("state", "OK");
        jsonObject.put("rawData", jsonRaw.toJSONString());
        mapSensorResult.put("node1", jsonObject);
        SessionContext.setAttribute(SessionParams.RAW_DATA, mapSensorResult);
        SensorResult = rawFormulaSensor.execute(SessionContext);
        System.out.print(SensorResult.getRawData());
        value = Utils.getDouble(((JSONObject) (new JSONParser().parse(SensorResult.getRawData()))).get("formulaValue"));
        System.out.println("formula = " + formula);
        System.out.println("value = " + value);
        assertEquals(value, 0.0, 0.0001);

        jsonRaw.put("state", "NOK");
        jsonObject.put("rawData", jsonRaw.toJSONString());
        mapSensorResult.put("node1", jsonObject);
        SessionContext.setAttribute(SessionParams.RAW_DATA, mapSensorResult);
        SensorResult = rawFormulaSensor.execute(SessionContext);
        System.out.print(SensorResult.getRawData());
        value = Utils.getDouble(((JSONObject) (new JSONParser().parse(SensorResult.getRawData()))).get("formulaValue"));
        System.out.println("formula = " + formula);
        System.out.println("value = " + value);
        assertEquals(value, 1.0, 0.0001);
    }

    @Test
    public void testSensorTemplateAndCount2() throws Exception {
//        LocationSensor locationSensor = new LocationSensor();
//        locationSensor.setProperty(LocationSensor.LONGITUDE, 19.851858);
//        locationSensor.setProperty(LocationSensor.LATITUDE, 45.262231);
//        locationSensor.setProperty(LocationSensor.DISTANCE, 100);


        SessionContext testSessionContext = new SessionContext(1);
        testSessionContext.setAttribute(LocationSensor.RUNTIME_LONGITUDE, 19.851858);
        testSessionContext.setAttribute(LocationSensor.RUNTIME_LATITUDE, 45.262231);

        String rawData = "{\"zip\":null,\"current_street\":\"Filipa Višnjića\",\"country\":\"Serbia\",\"current_street_number\":\"not found \",\"distance\":0.0,\"city\":\"Novi Sad\",\"latitude\":45.262231,\"street_name\":\"Filipa Višnjića\",\"current_country\":\"Serbia\",\"current_city\":\"Novi Sad\",\"street_number\":null,\"region\":\"Vojvodina\",\"longitude\":19.851858}";


        JSONObject node2RawData = new JSONObject();
        node2RawData.put("rawData", rawData);

        Map<String, Object> mapTestResult = new HashMap<>();
        mapTestResult.put("node2", node2RawData);
        testSessionContext.setAttribute(SessionParams.RAW_DATA, mapTestResult);

        RawFormulaSensor rawFormulaSensor = new RawFormulaSensor();
        rawFormulaSensor.setProperty("formula", "<count(Novi%, node2.rawData.current_city)>");
        rawFormulaSensor.setProperty("threshold", 1);
        SensorResult testResult = rawFormulaSensor.execute(testSessionContext);
        assertEquals("Equal", testResult.getObserverState());
        rawFormulaSensor.execute(testSessionContext);
        rawFormulaSensor.execute(testSessionContext);
        testResult = rawFormulaSensor.execute(testSessionContext);
        rawFormulaSensor.setProperty("threshold", 4);
        System.out.println(testResult.getRawData());
        assertEquals("Equal", testResult.getObserverState());


        mapTestResult.put("node2", node2RawData);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("current_city", "Gent");
        node2RawData.put("rawData", jsonObject);
        testSessionContext.setAttribute(SessionParams.RAW_DATA, mapTestResult);
        testResult = rawFormulaSensor.execute(testSessionContext);
        System.out.println(testResult.getRawData());
        assertEquals("Equal", testResult.getObserverState());

        testResult = rawFormulaSensor.execute(testSessionContext);
        assertEquals("Equal", testResult.getObserverState());

        testResult = rawFormulaSensor.execute(testSessionContext);
        assertEquals("Equal", testResult.getObserverState());

    }

    @Test
    public void testSensorTemplateAndCountWithWindow() throws Exception {
//        LocationSensor locationSensor = new LocationSensor();
//        locationSensor.setProperty(LocationSensor.LONGITUDE, 19.851858);
//        locationSensor.setProperty(LocationSensor.LATITUDE, 45.262231);
//        locationSensor.setProperty(LocationSensor.DISTANCE, 100);

        SessionContext testSessionContext = new SessionContext(1);
        testSessionContext.setAttribute(LocationSensor.RUNTIME_LONGITUDE, 19.851858);
        testSessionContext.setAttribute(LocationSensor.RUNTIME_LATITUDE, 45.262231);

        String rawData = "{\"zip\":null,\"current_street\":\"Filipa Višnjića\",\"country\":\"Serbia\",\"current_street_number\":\"not found \",\"distance\":0.0,\"city\":\"Novi Sad\",\"latitude\":45.262231,\"street_name\":\"Filipa Višnjića\",\"current_country\":\"Serbia\",\"current_city\":\"Novi Sad\",\"street_number\":null,\"region\":\"Vojvodina\",\"longitude\":19.851858}";

        JSONObject node3RawData = new JSONObject();
        node3RawData.put("rawData", rawData);

        Map<String, Object> mapTestResult = new HashMap<>();
        mapTestResult.put("node3", node3RawData);
        testSessionContext.setAttribute(SessionParams.RAW_DATA, mapTestResult);

        RawFormulaSensor rawFormulaSensor = new RawFormulaSensor();
        rawFormulaSensor.setProperty("formula", "<count(Novi,3,minutes, node3.rawData.current_city)>");
        rawFormulaSensor.setProperty("threshold", 1);
        SensorResult testResult = rawFormulaSensor.execute(testSessionContext);
        assertEquals("Equal", testResult.getObserverState());
        rawFormulaSensor.execute(testSessionContext);
        testResult = rawFormulaSensor.execute(testSessionContext);
        rawFormulaSensor.setProperty("threshold", 3);
        System.out.println(testResult.getRawData());
        assertEquals("Equal", testResult.getObserverState());

    }

    @Test
    public void testLocationsAndFormula() throws ParseException {
//        LocationRawSensor locationRawSensor1 = new LocationRawSensor();
//        locationRawSensor1.setProperty("location", "Gent");
//
//        LocationRawSensor locationRawSensor2 = new LocationRawSensor();
//        locationRawSensor2.setProperty("location", "Novi Sad");

        SessionContext testSessionContext = new SessionContext(1);

        Map<String, Object> mapTestResult = new HashMap<>();
        mapTestResult.put("node1", new JSONParser().parse("{\"latitude\":51.0543422,\"longitude\":3.7174243}"));
        mapTestResult.put("node2", new JSONParser().parse("{\"latitude\":45.25,\"longitude\":19.85}"));

        testSessionContext.setAttribute(SessionParams.RAW_DATA, mapTestResult);

        RawFormulaSensor rawFormulaSensor = new RawFormulaSensor();
        String formula = "<node1.latitude> - <node2.latitude>";
        rawFormulaSensor.setProperty("formula", formula);
        rawFormulaSensor.setProperty("threshold", "5");

        testSessionContext.setAttribute(SessionParams.RAW_DATA, mapTestResult);
        SensorResult testResult = rawFormulaSensor.execute(testSessionContext);
        assertEquals("Above", testResult.getObserverState());

        rawFormulaSensor = new RawFormulaSensor();
        formula = "distance(node1, node2) - 500";
        rawFormulaSensor.setProperty("formula", formula);
        rawFormulaSensor.setProperty("threshold", "1000");

        testSessionContext.setAttribute(SessionParams.RAW_DATA, mapTestResult);
        testResult = rawFormulaSensor.execute(testSessionContext);
        assertEquals("Below", testResult.getObserverState());
        assertEquals(855, Utils.getDouble(((JSONObject) (new JSONParser().parse(testResult.getRawData()))).get("formulaValue")), 10);
    }

    @Test
    public void testSensorTemplateAndCount() throws Exception {
//        LocationSensor locationSensor = new LocationSensor();
//        locationSensor.setProperty(LocationSensor.LONGITUDE, 19.851858);
//        locationSensor.setProperty(LocationSensor.LATITUDE, 45.262231);
//        locationSensor.setProperty(LocationSensor.DISTANCE, 100);

        SessionContext testSessionContext = new SessionContext(1);
        testSessionContext.setAttribute(LocationSensor.RUNTIME_LONGITUDE, 19.851858);
        testSessionContext.setAttribute(LocationSensor.RUNTIME_LATITUDE, 45.262231);

        String rawData = "{\"zip\":null,\"current_street\":\"Filipa Višnjića\",\"country\":\"Serbia\",\"current_street_number\":\"not found \",\"distance\":0.0,\"city\":\"Novi Sad\",\"latitude\":45.262231,\"street_name\":\"Filipa Višnjića\",\"current_country\":\"Serbia\",\"current_city\":\"Novi Sad\",\"street_number\":null,\"region\":\"Vojvodina\",\"longitude\":19.851858}";

        JSONObject node1rawData = new JSONObject();
        node1rawData.put("rawData", rawData);

        Map<String, Object> mapTestResult = new HashMap<>();
        mapTestResult.put("node1", node1rawData);
        testSessionContext.setAttribute(SessionParams.RAW_DATA, mapTestResult);

        RawFormulaSensor rawFormulaSensor = new RawFormulaSensor();
        rawFormulaSensor.setProperty("formula", "<count(Novi,3,samples, node1.rawData.current_city)>");
        rawFormulaSensor.setProperty("threshold", 1);
        SensorResult testResult = rawFormulaSensor.execute(testSessionContext);
        assertEquals("Equal", testResult.getObserverState());
        rawFormulaSensor.execute(testSessionContext);
        rawFormulaSensor.execute(testSessionContext);
        testResult = rawFormulaSensor.execute(testSessionContext);
        rawFormulaSensor.setProperty("threshold", 3);
        System.out.println(testResult.getRawData());
        assertEquals("Equal", testResult.getObserverState());


        mapTestResult.put("node1", node1rawData);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("current_city", "Gent");

        node1rawData.put("rawData", jsonObject);
        testSessionContext.setAttribute(SessionParams.RAW_DATA, mapTestResult);
        testResult = rawFormulaSensor.execute(testSessionContext);
        rawFormulaSensor.setProperty("threshold", 2);
        System.out.println(testResult.getRawData());
        assertEquals("Equal", testResult.getObserverState());

        testResult = rawFormulaSensor.execute(testSessionContext);
        rawFormulaSensor.setProperty("threshold", 1);
        assertEquals("Equal", testResult.getObserverState());

        testResult = rawFormulaSensor.execute(testSessionContext);
        rawFormulaSensor.setProperty("threshold", 0);
        assertEquals("Equal", testResult.getObserverState());

    }
}
