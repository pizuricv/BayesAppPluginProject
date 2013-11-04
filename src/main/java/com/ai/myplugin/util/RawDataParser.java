package com.ai.myplugin.util;

import com.ai.util.resource.NodeSessionParams;
import com.ai.util.resource.TestSessionContext;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by User: veselin
 * On Date: 04/11/13
 */
public class RawDataParser {

    /**
     *  format node1->param1
     * @throws org.json.simple.parser.ParseException
     */
    public static String parse(Map<String, Object> nodeParams, String stringToParse) throws ParseException {
        String returnString = stringToParse;
        String [] split = returnString.split("\\s+");
        Map<String, String> map = new ConcurrentHashMap<String, String>();
        for(String s1 : split)   {
            String [] s2 = s1.split("->");
            if(s2.length == 2)  {
                String node = s2[0];
                String value = s2[1];
                JSONObject jsonObject = (JSONObject) (nodeParams.get(node));
                Object rawValue =  ((JSONObject) new JSONParser().parse((String) jsonObject.get("rawData"))).get(value);
                map.put(s1, rawValue.toString());
            }
        }

        for(Map.Entry<String, String> entry: map.entrySet()){
            returnString = returnString.replaceAll(entry.getKey(), entry.getValue());
        }
        return returnString;
    };


    public static String parseNodesData(TestSessionContext testSessionContext) {
        String target = (String) testSessionContext.getAttribute(NodeSessionParams.TARGET_NODE);
        String targetState = (String) testSessionContext.getAttribute(NodeSessionParams.TARGET_STATE);
        String node = (String) testSessionContext.getAttribute(NodeSessionParams.NODE_NAME);
        String nodeState = (String) testSessionContext.getAttribute(NodeSessionParams.NODE_TRIGGERED_STATE);
        String nodeAction = (String) testSessionContext.getAttribute(NodeSessionParams.ACTION_NODE);
        return "\nTarget "+target + " in state: " + targetState + "\n" + "Node "+ node + " in state: " + nodeState + "\n" +
                "Node that triggered the action: "+ nodeAction;
    }
}
