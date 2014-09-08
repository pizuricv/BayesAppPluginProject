/**
 * Created by User: veselin
 * On Date: 04/11/13
 */

package com.ai.myplugin.util;

import com.ai.api.SessionContext;
import com.ai.api.SessionParams;
import com.ai.myplugin.sensor.LocationSensor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.*;

import static org.junit.Assert.assertEquals;

public class RawDataParserTest{

    @Test
    public void testParser() throws ParseException {
        Map<String, Object> mapTestResult = new HashMap<String, Object>();
        JSONObject objRaw = new JSONObject();
        objRaw.put("value1", 1);
        objRaw.put("hello", "hello vele");
        mapTestResult.put("node1", objRaw);

        objRaw = new JSONObject();
        objRaw.put("value2", 1);
        objRaw.put("time", 1234);
        mapTestResult.put("node2", objRaw);

        String test = RawDataParser.parseTemplateFromRawMap("Hello World <node2.value2>", mapTestResult);
        assertTrue("Hello World 1".equals(test));

        test = RawDataParser.parseTemplateFromRawMap("Hello World <node1.hello>", mapTestResult);
        assertTrue("Hello World hello vele".equals(test));

        test = RawDataParser.parseTemplateFromRawMap("Hello World...", mapTestResult);
        assertTrue("Hello World...".equals(test));

        JSONArray jsonArray = new JSONArray();
        objRaw = new JSONObject();
        objRaw.put("value3", 1);
        objRaw.put("time", 1234);
        jsonArray.add(objRaw);

        objRaw = new JSONObject();
        objRaw.put("value3", 2);
        objRaw.put("time", 1234);
        jsonArray.add(objRaw);

        objRaw = new JSONObject();
        objRaw.put("value3", 3);
        objRaw.put("time", 1234);
        jsonArray.add(objRaw);

        objRaw  = new JSONObject();
        objRaw.put("x", "hello");
        jsonArray.add(objRaw);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("array", jsonArray);

        mapTestResult.put("node3", jsonObject);


        test = RawDataParser.parseTemplateFromRawMap("Hello World <node3.array.2.value3>", mapTestResult);
        assertEquals("Hello World 3", test);

        test = RawDataParser.parseTemplateFromRawMap("Hello World <node3.array.first.value3>", mapTestResult);
        assertEquals("Hello World 1", test);

        test = RawDataParser.parseTemplateFromRawMap("Hello World <node3.array.last.x>", mapTestResult);
        assertEquals("Hello World hello", test);

        test = RawDataParser.parseTemplateFromRawMap("Hello World <node3.array>", mapTestResult);
        assertEquals("Hello World [{\"value3\":1,\"time\":1234},{\"value3\":2,\"time\":1234},{\"value3\":3,\"time\":1234},{\"x\":\"hello\"}]", test);
    }

    @Test
    public void testTemplate() {
        Set set = RawDataParser.parseKeyArgs("Hello World <node2> dsfsdf <value2> if x <3  then  hello");
        assertEquals(2, set.size());
        assertEquals("[[node2, value2]]", Arrays.asList(set).toString());
    }

    @Test
    public void testTemplate2() {
        Set set = RawDataParser.getRuntimePropertiesFromTemplate("Hello World <runtime_node2> dsfsdf <runtime_value2> if x <3  then  hello", "runtime_");
        assertEquals(2, set.size());
        assertEquals("[[runtime_node2, runtime_value2]]", Arrays.asList(set).toString());

    }


    @Test
    public void testSensorTemplate() throws Exception {
//        LocationSensor locationSensor = new LocationSensor();
//        locationSensor.setProperty(LocationSensor.LONGITUDE, 19.851858);
//        locationSensor.setProperty(LocationSensor.LATITUDE, 45.262231);
//        locationSensor.setProperty(LocationSensor.DISTANCE, 100);
//
        SessionContext testSessionContext = new SessionContext(1);
//        testSessionContext.setAttribute(LocationSensor.RUNTIME_LONGITUDE, 19.851858);
//        testSessionContext.setAttribute(LocationSensor.RUNTIME_LATITUDE, 45.262231);


        String rawData = "{\"zip\":null,\"current_street\":\"Filipa Višnjića\",\"country\":\"Serbia\",\"current_street_number\":\"not found \",\"distance\":0.0,\"city\":\"Novi Sad\",\"latitude\":45.262231,\"street_name\":\"Filipa Višnjića\",\"current_country\":\"Serbia\",\"current_city\":\"Novi Sad\",\"street_number\":null,\"region\":\"Vojvodina\",\"longitude\":19.851858}";

        JSONObject node1rawData = new JSONObject();
        node1rawData.put("rawData", rawData);

        Map<String, Object> mapTestResult = new HashMap<>();
        mapTestResult.put("node1", node1rawData);
        testSessionContext.setAttribute(SessionParams.RAW_DATA, mapTestResult);
        String template = "Location is <node1.rawData.current_country>, <node1.rawData.current_city>, <node1.rawData.current_street>";
        String result = RawDataParser.parseTemplateFromContext(template, testSessionContext);
        assertEquals("Location is Serbia, Novi Sad, Filipa Višnjića", result);
    }


}
