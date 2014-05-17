/**
 * Created by User: veselin
 * On Date: 04/11/13
 */

package com.ai.myplugin.util;

import junit.framework.TestCase;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import java.util.*;

public class RawDataParserTest extends TestCase {

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
        assertTrue("Hello World 3".equals(test));

        test = RawDataParser.parseTemplateFromRawMap("Hello World <node3.array.first.value3>", mapTestResult);
        assertTrue("Hello World 1".equals(test));

        test = RawDataParser.parseTemplateFromRawMap("Hello World <node3.array.last.x>", mapTestResult);
        assertTrue("Hello World hello".equals(test));

        test = RawDataParser.parseTemplateFromRawMap("Hello World <node3.array>", mapTestResult);
        assertTrue("Hello World {\"time\":1234,\"value3\":1}".equals(test));
    }

    public void testTemplate() {
        Set set = RawDataParser.parseKeyArgs("Hello World <node2> dsfsdf <value2> if x <3  then  hello");
        assertEquals(2, set.size());
        assertEquals("[[node2, value2]]", Arrays.asList(set).toString());

    }

    public void testTemplate2() {
        Set set = RawDataParser.getRuntimePropertiesFromTemplate("Hello World <runtime_node2> dsfsdf <runtime_value2> if x <3  then  hello", "runtime_");
        assertEquals(2, set.size());
        assertEquals("[[runtime_node2, runtime_value2]]", Arrays.asList(set).toString());

    }

}
