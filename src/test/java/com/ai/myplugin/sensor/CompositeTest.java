/**
 * Created by User: veselin
 * On Date: 24/12/13
 */

package com.ai.myplugin.sensor;

import com.ai.api.SensorResult;
import com.ai.api.SessionContext;
import com.ai.api.SessionParams;
import com.ai.myplugin.util.RawDataParser;
import com.ai.myplugin.util.Utils;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

public class CompositeTest{

    @Test
    public void testLocationsAndFormula() throws ParseException {
        LocationRawSensor locationRawSensor1 = new LocationRawSensor();
        locationRawSensor1.setProperty("location", "Gent");

        LocationRawSensor locationRawSensor2 = new LocationRawSensor();
        locationRawSensor2.setProperty("location", "Novi Sad");

        SessionContext testSessionContext = new SessionContext(1);

        JSONObject jsonObject1 = new JSONObject();

        jsonObject1.put("rawData", locationRawSensor1.execute(null).getRawData());
        Map<String, Object> mapTestResult = new HashMap<>();
        mapTestResult.put("node1", jsonObject1);

        JSONObject jsonObject2 = new JSONObject();
        jsonObject2.put("rawData", locationRawSensor2.execute(null).getRawData());
        mapTestResult.put("node2", jsonObject2);

        testSessionContext.setAttribute(SessionParams.RAW_DATA, mapTestResult);

        RawFormulaSensor rawFormulaSensor = new RawFormulaSensor();
        String formula = "<node1.rawData.latitude> - <node2.rawData.latitude>";
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
    public void testSensorTemplate() throws Exception {
        LocationSensor locationSensor = new LocationSensor();
        locationSensor.setProperty(LocationSensor.LONGITUDE, 19.851858);
        locationSensor.setProperty(LocationSensor.LATITUDE, 45.262231);
        locationSensor.setProperty(LocationSensor.DISTANCE, 100);
        SessionContext testSessionContext = new SessionContext(1);
        testSessionContext.setAttribute(LocationSensor.RUNTIME_LONGITUDE, 19.851858);
        testSessionContext.setAttribute(LocationSensor.RUNTIME_LATITUDE, 45.262231);

        JSONObject jsonObject1 = new JSONObject();

        jsonObject1.put("rawData", locationSensor.execute(testSessionContext).getRawData());
        Map<String, Object> mapTestResult = new HashMap<String, Object>();
        mapTestResult.put("node1", jsonObject1);
        testSessionContext.setAttribute(SessionParams.RAW_DATA, mapTestResult);
        String template = "Location is <node1.rawData.current_country>, <node1.rawData.current_city>, <node1.rawData.current_street>";
        System.out.println(RawDataParser.parseTemplateFromContext(template, testSessionContext));

    }

    @Test
    public void testSensorTemplateAndCount() throws Exception {
        LocationSensor locationSensor = new LocationSensor();
        locationSensor.setProperty(LocationSensor.LONGITUDE, 19.851858);
        locationSensor.setProperty(LocationSensor.LATITUDE, 45.262231);
        locationSensor.setProperty(LocationSensor.DISTANCE, 100);
        SessionContext testSessionContext = new SessionContext(1);
        testSessionContext.setAttribute(LocationSensor.RUNTIME_LONGITUDE, 19.851858);
        testSessionContext.setAttribute(LocationSensor.RUNTIME_LATITUDE, 45.262231);

        JSONObject jsonObject1 = new JSONObject();

        jsonObject1.put("rawData", locationSensor.execute(testSessionContext).getRawData());
        Map<String, Object> mapTestResult = new HashMap<String, Object>();
        mapTestResult.put("node1", jsonObject1);
        testSessionContext.setAttribute(SessionParams.RAW_DATA, mapTestResult);

        RawFormulaSensor rawFormulaSensor = new RawFormulaSensor();
        rawFormulaSensor.setProperty("formula", "<count(Novi Sad,3,samples, node1.rawData.current_city)>");
        rawFormulaSensor.setProperty("threshold", 1);
        SensorResult testResult = rawFormulaSensor.execute(testSessionContext);
        assertEquals("Equal", testResult.getObserverState());
        rawFormulaSensor.execute(testSessionContext);
        rawFormulaSensor.execute(testSessionContext);
        testResult = rawFormulaSensor.execute(testSessionContext);
        rawFormulaSensor.setProperty("threshold", 3);
        System.out.println(testResult.getRawData());
        assertEquals("Equal", testResult.getObserverState());


        mapTestResult.put("node1", jsonObject1);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("current_city", "Gent");
        jsonObject1.put("rawData", jsonObject);
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

    @Test
    public void testSensorTemplateAndCount2() throws Exception {
        LocationSensor locationSensor = new LocationSensor();
        locationSensor.setProperty(LocationSensor.LONGITUDE, 19.851858);
        locationSensor.setProperty(LocationSensor.LATITUDE, 45.262231);
        locationSensor.setProperty(LocationSensor.DISTANCE, 100);
        SessionContext testSessionContext = new SessionContext(1);
        testSessionContext.setAttribute(LocationSensor.RUNTIME_LONGITUDE, 19.851858);
        testSessionContext.setAttribute(LocationSensor.RUNTIME_LATITUDE, 45.262231);

        JSONObject jsonObject1 = new JSONObject();

        jsonObject1.put("rawData", locationSensor.execute(testSessionContext).getRawData());
        Map<String, Object> mapTestResult = new HashMap<String, Object>();
        mapTestResult.put("node2", jsonObject1);
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


        mapTestResult.put("node2", jsonObject1);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("current_city", "Gent");
        jsonObject1.put("rawData", jsonObject);
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
        LocationSensor locationSensor = new LocationSensor();
        locationSensor.setProperty(LocationSensor.LONGITUDE, 19.851858);
        locationSensor.setProperty(LocationSensor.LATITUDE, 45.262231);
        locationSensor.setProperty(LocationSensor.DISTANCE, 100);
        SessionContext testSessionContext = new SessionContext(1);
        testSessionContext.setAttribute(LocationSensor.RUNTIME_LONGITUDE, 19.851858);
        testSessionContext.setAttribute(LocationSensor.RUNTIME_LATITUDE, 45.262231);

        JSONObject jsonObject1 = new JSONObject();

        jsonObject1.put("rawData", locationSensor.execute(testSessionContext).getRawData());
        Map<String, Object> mapTestResult = new HashMap<>();
        mapTestResult.put("node3", jsonObject1);
        testSessionContext.setAttribute(SessionParams.RAW_DATA, mapTestResult);

        RawFormulaSensor rawFormulaSensor = new RawFormulaSensor();
        rawFormulaSensor.setProperty("formula", "<count(Novi Sad,3,minutes, node3.rawData.current_city)>");
        rawFormulaSensor.setProperty("threshold", 1);
        SensorResult testResult = rawFormulaSensor.execute(testSessionContext);
        assertEquals("Equal", testResult.getObserverState());
        rawFormulaSensor.execute(testSessionContext);
        testResult = rawFormulaSensor.execute(testSessionContext);
        rawFormulaSensor.setProperty("threshold", 3);
        System.out.println(testResult.getRawData());
        assertEquals("Equal", testResult.getObserverState());

    }


}
