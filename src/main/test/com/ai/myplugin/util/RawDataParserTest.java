package com.ai.myplugin.util;

import junit.framework.TestCase;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.util.HashMap;
import java.util.Map;

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
}
