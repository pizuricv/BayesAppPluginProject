package com.ai.myplugin.sensor;

import com.ai.bayes.scenario.TestResult;
import com.ai.myplugin.util.RawDataParser;
import com.ai.myplugin.util.Utils;
import com.ai.util.resource.NodeSessionParams;
import com.ai.util.resource.TestSessionContext;
import junit.framework.TestCase;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by User: veselin
 * On Date: 24/12/13
 */
public class CompositeTest extends TestCase{

    public void testLocationAndFormula() throws ParseException {
        LatitudeLongitudeRawSensor locationSensor = new LatitudeLongitudeRawSensor();
        locationSensor.setProperty("longitude", 19.851858);
        locationSensor.setProperty("latitude", 45.262231);

        TestSessionContext testSessionContext = new TestSessionContext(1);
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("rawData", locationSensor.execute(null).getRawData());
        Map<String, Object> mapTestResult = new HashMap<String, Object>();
        mapTestResult.put("node1", jsonObject);
        testSessionContext.setAttribute(NodeSessionParams.RAW_DATA, mapTestResult);

        RawFormulaSensor rawFormulaSensor = new RawFormulaSensor();
        String formula = "node1->city~Novi Sad";
        rawFormulaSensor.setProperty("formula", formula);
        rawFormulaSensor.setProperty("threshold", "1");

        testSessionContext.setAttribute(NodeSessionParams.RAW_DATA, mapTestResult);
        TestResult testResult = rawFormulaSensor.execute(testSessionContext);
        double value = Utils.getDouble(((JSONObject) (new JSONParser().parse(testResult.getRawData()))).get("formulaValue"));
        assertEquals("Equal", testResult.getObserverState());

        assertEquals(value, 1.0);

        formula = "node1->country~Serbia";
        rawFormulaSensor.setProperty("formula", formula);
        rawFormulaSensor.setProperty("threshold", "0");

        testSessionContext.setAttribute(NodeSessionParams.RAW_DATA, mapTestResult);
        testResult = rawFormulaSensor.execute(testSessionContext);
        value = Utils.getDouble(((JSONObject) (new JSONParser().parse(testResult.getRawData()))).get("formulaValue"));
        assertEquals("Above", testResult.getObserverState());

        assertEquals(value, 1.0);
    }


    public void testLocationsAndFormula() throws ParseException {
        LocationRawSensor locationRawSensor1 = new LocationRawSensor();
        locationRawSensor1.setProperty("location", "Gent");

        LocationRawSensor locationRawSensor2 = new LocationRawSensor();
        locationRawSensor2.setProperty("location", "Novi Sad");

        TestSessionContext testSessionContext = new TestSessionContext(1);

        JSONObject jsonObject1 = new JSONObject();

        jsonObject1.put("rawData", locationRawSensor1.execute(null).getRawData());
        Map<String, Object> mapTestResult = new HashMap<String, Object>();
        mapTestResult.put("node1", jsonObject1);

        JSONObject jsonObject2 = new JSONObject();
        jsonObject2.put("rawData", locationRawSensor2.execute(null).getRawData());
        mapTestResult.put("node2", jsonObject2);

        testSessionContext.setAttribute(NodeSessionParams.RAW_DATA, mapTestResult);

        RawFormulaSensor rawFormulaSensor = new RawFormulaSensor();
        String formula = "node1->city~Ghent";
        rawFormulaSensor.setProperty("formula", formula);
        rawFormulaSensor.setProperty("threshold", "1");

        testSessionContext.setAttribute(NodeSessionParams.RAW_DATA, mapTestResult);
        TestResult testResult = rawFormulaSensor.execute(testSessionContext);
        double value = Utils.getDouble(((JSONObject) (new JSONParser().parse(testResult.getRawData()))).get("formulaValue"));
        assertEquals("Equal", testResult.getObserverState());

        assertEquals(value, 1.0);

        formula = "node1->country~Belgium";
        rawFormulaSensor.setProperty("formula", formula);
        rawFormulaSensor.setProperty("threshold", "0");

        testSessionContext.setAttribute(NodeSessionParams.RAW_DATA, mapTestResult);
        testResult = rawFormulaSensor.execute(testSessionContext);
        value = Utils.getDouble(((JSONObject) (new JSONParser().parse(testResult.getRawData()))).get("formulaValue"));
        assertEquals("Above", testResult.getObserverState());

        assertEquals(value, 1.0);


        rawFormulaSensor = new RawFormulaSensor();
        formula = "<node1.rawData.latitude> - <node2.rawData.latitude>";
        rawFormulaSensor.setProperty("formula", formula);
        rawFormulaSensor.setProperty("threshold", "5");

        testSessionContext.setAttribute(NodeSessionParams.RAW_DATA, mapTestResult);
        testResult = rawFormulaSensor.execute(testSessionContext);
        //System.out.println(Utils.getDouble(((JSONObject) (new JSONParser().parse(testResult.getRawData()))).get("formulaValue")));
        assertEquals("Above", testResult.getObserverState());

        /*jsonObject1 = (JSONObject) new JSONParser().parse(jsonObject1.get("rawData").toString());
        jsonObject2 = (JSONObject) new JSONParser().parse(jsonObject2.get("rawData").toString());
        System.out.println("Distance: "+ FormulaParser.calculateDistance(Utils.getDouble(jsonObject1.get("latitude")),
                Utils.getDouble(jsonObject1.get("longitude")), Utils.getDouble(jsonObject2.get("latitude")),
                Utils.getDouble(jsonObject2.get("longitude"))));    */


        rawFormulaSensor = new RawFormulaSensor();
        formula = "distance(node1, node2) - 500";
        rawFormulaSensor.setProperty("formula", formula);
        rawFormulaSensor.setProperty("threshold", "1000");

        testSessionContext.setAttribute(NodeSessionParams.RAW_DATA, mapTestResult);
        testResult = rawFormulaSensor.execute(testSessionContext);
        assertEquals("Below", testResult.getObserverState());
        assertEquals(855, Utils.getDouble(((JSONObject) (new JSONParser().parse(testResult.getRawData()))).get("formulaValue")), 10);
    }


    public void testSensorTemplate() throws Exception {
        LocationSensor locationSensor = new LocationSensor();
        locationSensor.setProperty(LocationSensor.LONGITUDE, 19.851858);
        locationSensor.setProperty(LocationSensor.LATITUDE, 45.262231);
        locationSensor.setProperty(LocationSensor.DISTANCE, 100);
        TestSessionContext testSessionContext = new TestSessionContext(1);
        testSessionContext.setAttribute(LocationSensor.RUNTIME_LONGITUDE, 19.851858);
        testSessionContext.setAttribute(LocationSensor.RUNTIME_LATITUDE, 45.262231);

        JSONObject jsonObject1 = new JSONObject();

        jsonObject1.put("rawData", locationSensor.execute(testSessionContext).getRawData());
        Map<String, Object> mapTestResult = new HashMap<String, Object>();
        mapTestResult.put("node1", jsonObject1);
        testSessionContext.setAttribute(NodeSessionParams.RAW_DATA, mapTestResult);
        String template = "Location is <node1.rawData.current_country>, <node1.rawData.current_city>, <node1.rawData.current_street>";
        System.out.println(RawDataParser.parseTemplateFromContext(template, testSessionContext));

    }


}
