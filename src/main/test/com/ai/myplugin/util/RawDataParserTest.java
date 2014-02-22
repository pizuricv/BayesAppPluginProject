package com.ai.myplugin.util;

import junit.framework.TestCase;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.util.*;

/**
 * Created by User: veselin
 * On Date: 04/11/13
 */
public class RawDataParserTest extends TestCase {

    public void testParser() throws ParseException {
        Map<String, Object> mapTestResult = new HashMap<String, Object>();
        JSONObject objRaw = new JSONObject();
        objRaw.put("value1", 1);
        objRaw.put("hello", "hello vele");
        objRaw.put("rawData", objRaw.toJSONString());
        mapTestResult.put("node1", objRaw);

        objRaw = new JSONObject();
        objRaw.put("value2", 1);
        objRaw.put("time", 1234);
        objRaw.put("rawData", objRaw.toJSONString());
        mapTestResult.put("node2", objRaw);

        String test = RawDataParser.parse(mapTestResult, "Hello World node2->value2");
        assertTrue("Hello World 1".equals(test));

        test = RawDataParser.parse(mapTestResult, "Hello World node1->hello");
        assertTrue("Hello World hello vele".equals(test));

        test = RawDataParser.parse(mapTestResult, "Hello World...");
        assertTrue("Hello World...".equals(test));
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
