package com.ai.myplugin.util;

import com.ai.util.resource.NodeSessionParams;
import com.ai.util.resource.TestSessionContext;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.stringtemplate.v4.ST;

import java.util.*;
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

    public static String parseTemplateFromContext(String template, TestSessionContext testSessionContext){
        Set<String> set = parseKeyArgs(template);
        Map<String, String> map = new ConcurrentHashMap<String, String>();
        Map sessionMap = (Map) testSessionContext.getAttribute(NodeSessionParams.RAW_DATA);
        JSONObject jsonObject = new JSONObject(sessionMap);
        String nodeKey;
        for(String key: set){
            if(key.indexOf(".") > -1)   {
                nodeKey = key.substring(0, key.indexOf("."));
                JSONObject jso;
                if(jsonObject.get(nodeKey) != null){
                    Object obj = null;
                    //first node must be a json
                    jso = (JSONObject) jsonObject.get(nodeKey);
                    String delims = ".";
                    StringTokenizer tokens = new StringTokenizer(key, delims);
                    tokens.nextToken();
                    while (tokens.hasMoreTokens()){
                        obj = jso.get(tokens.nextElement());
                        if(obj instanceof JSONObject)
                            jso = (JSONObject) obj;
                        else if(obj instanceof JSONArray)
                            jso = (JSONObject) ((JSONArray) obj).get(0);
                        else {
                            try{
                                jso = (JSONObject) new JSONParser().parse(obj.toString());
                            } catch (Exception e){
                                break;
                            }
                        }
                    }
                    if(obj != null)
                        map.put(key, obj.toString());
                }

            }
        }
        for(String key : map.keySet()) {
            template = template.replaceAll(key, key.replaceAll("\\.",""));
        }
        ST hello = new ST(template);
        for(String key : map.keySet()) {
            hello.add(key.replaceAll("\\.",""), map.get(key));
        }
        return hello.render();
    };

    public static Set<String> parseKeyArgs(String template){
        String delims = ">";
        Set<String> set = new HashSet<String>();
        StringTokenizer tokens = new StringTokenizer(template, delims);
        while (tokens.hasMoreTokens()){
            String str = tokens.nextToken();
            if(str.indexOf("<") > -1)
                str = str.substring(str.indexOf("<") + 1);
            if(str.indexOf(" ") == -1)
                set.add(str);
        }
        return set;
    };

    public static Set<String> getRuntimePropertiesFromTemplate(String template, String startString){
        Set<String> set = parseKeyArgs(template);
        Set<String> ret = new HashSet<String>();
        for(String key : set)
            if(key.contains(startString) ||  key.contains(startString.toLowerCase()) ||
                    key.contains(startString.toUpperCase()))
                ret.add(key);
        return ret;
    };
}
