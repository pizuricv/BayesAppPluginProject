package com.ai.myplugin.util;

import com.ai.util.resource.NodeSessionParams;
import com.ai.util.resource.TestSessionContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
    private static final Log log = LogFactory.getLog(RawDataParser.class);

    public static String parseTemplateFromContext(String template, TestSessionContext testSessionContext){
        return parseTemplateFromRawMap(template, (Map) testSessionContext.getAttribute(NodeSessionParams.RAW_DATA));
    };

    public static String parseTemplateFromRawMap(String template, Map sessionMap){
        log.debug("parseTemplateFromRawMap " + template);
        Set<String> set = parseKeyArgs(template);
        Map<String, String> map = new ConcurrentHashMap<String, String>();
        JSONObject jsonObject = new JSONObject(sessionMap);
        for(String key: set){
            Object obj = findObjForKey(key, jsonObject);
            if(obj != null)
                map.put(key, obj.toString());
        }
        //template keys can't have dots
        for(String key : map.keySet()) {
            template = template.replaceAll(key, key.replaceAll("\\.",""));
        }
        ST hello = new ST(template);
        for(String key : map.keySet()) {
            hello.add(key.replaceAll("\\.",""), map.get(key));
        }
        return hello.render();
    }

    public static Object findObjForKey(String key, JSONObject jsonObject) {
        return findObjForKey(key, jsonObject, null);
    }

    /**
     *
     * @param key example: node1.name.value , if the value is an array, it needs to continue like:
     *        node1.name.value.first.value2 OR node1.name.value.last.value2 OR node1.name.value.#number.value2
     *            if nothing given after array, it will return the first element
     * @param jsonObject    json object to be parsed
     * @param nodeSeparator separator between the node (node1) an the object (name.value), default is "."
     * @return object at the leave to which the key was pointing
     */
    private static Object findObjForKey(String key, JSONObject jsonObject, String nodeSeparator) {
        log.debug("findObjForKey "+key + " , "+jsonObject.toJSONString());
        if(nodeSeparator == null)
            nodeSeparator = ".";   //how the node is separated from the raw data . or -> for instance
        String delims = "."; //json notation for walking the graph
        String nodeKey;
        if(key.indexOf(nodeSeparator) > -1)   {
            nodeKey = key.substring(0, key.indexOf(nodeSeparator));
            key = key.substring(key.indexOf(nodeSeparator));
            JSONObject jso;
            if(jsonObject.get(nodeKey) != null){
                Object obj = null;
                //first node in the tree must be a json object, not an array
                jso = (JSONObject) jsonObject.get(nodeKey);
                StringTokenizer tokens = new StringTokenizer(key, delims);
                while (tokens.hasMoreTokens()){
                    obj = jso.get(tokens.nextElement());
                    if(obj instanceof JSONObject)
                        jso = (JSONObject) obj;
                    else if(obj instanceof JSONArray) {
                        if(!tokens.hasMoreTokens())  {
                            obj = ((JSONArray) obj).get(0);
                            break;
                        }
                        else{
                            String nextT = tokens.nextToken();
                            if(nextT.equalsIgnoreCase("last"))
                                jso = (JSONObject) ((JSONArray) obj).get(((JSONArray) obj).size()-1);
                            else {
                                try {
                                    Double num = Utils.getDouble(nextT);
                                    jso = (JSONObject) ((JSONArray) obj).get(num.intValue());
                                } catch (Exception e){
                                    jso = (JSONObject) ((JSONArray) obj).get(0);
                                }
                            }
                        }
                    }
                    else {
                        try{
                            jso = (JSONObject) new JSONParser().parse(obj.toString());
                        } catch (Exception e){
                            break;
                        }
                    }
                }
                if(obj != null){
                    log.debug("Found for " + nodeKey + "[" +key + "] = "+obj.toString()) ;
                    return obj;
                }
            }
        }
        return null;
    }


    /**
     * find strings that are between < >
     * @param template
     * @return
     */
    public static Set<String> parseKeyArgs(String template){
        log.debug("parseKeyArgs "+template);
        String delims = ">";
        Set<String> set = new HashSet<String>();
        StringTokenizer tokens = new StringTokenizer(template, delims);
        while (tokens.hasMoreTokens()){
            String str = tokens.nextToken();
            if(str.indexOf("<") > -1)
                str = str.substring(str.indexOf("<") + 1);
            if(str.indexOf(" ") == -1 || str.indexOf(",") > -1)
                set.add(str);
        }
        log.debug("found keys "+Arrays.asList(set).toString());
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


    public static String giveTargetNodeStateAsString(TestSessionContext testSessionContext) {
        String target = (String) testSessionContext.getAttribute(NodeSessionParams.TARGET_NODE);
        String targetState = (String) testSessionContext.getAttribute(NodeSessionParams.TARGET_STATE);
        String node = (String) testSessionContext.getAttribute(NodeSessionParams.NODE_NAME);
        String nodeState = (String) testSessionContext.getAttribute(NodeSessionParams.NODE_TRIGGERED_STATE);
        return "\n\nTarget "+target + " in the state: " + targetState + "\n" +
                "Node "+ node + " in the state: " + nodeState;
    }
}

