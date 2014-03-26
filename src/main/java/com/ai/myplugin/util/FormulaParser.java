package com.ai.myplugin.util;

import de.congrace.exp4j.Calculable;
import de.congrace.exp4j.ExpressionBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.stringtemplate.v4.ST;


import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by User: veselin
 * On Date: 28/10/13
 */
public class FormulaParser {
    private static final Log log = LogFactory.getLog(FormulaParser.class);
    static Map<String, ArrayList> prevValues = new ConcurrentHashMap();
    static Map<String, UtilStats> statValues = new ConcurrentHashMap();

    public static double executeFormula(String formula) throws Exception {
        log.debug("execute formula " + formula);
        Calculable calc = new ExpressionBuilder(formula).build();
        double res = calc.calculate();
        log.debug("result is " + res);
        return res;
    }

    /**
     *  formula in format <node1.param1> OPER <node1.param3> OPER <node2.param3>
     *  also can contain a previous values, such as <node1.param1>[-1] OPER <node2.param3>[-2]
     *  there is also the option to make a geo distance calculation such as distance(node1,node2)
     *  in which case it will be assumed that the raw data has this information (latitude and longitude)
     *  you can also add a stats in format <avg(node1.param1)>,<min(node1.param1)>, <max(node1.param1)>, <std(node1.param1)>
     *  or add a sliding window (by time or the number of samples): <avg(5, samples, node1.param1)>, <avg(5, min, node1.param1)>
     *  it can be only one window size per paramValue. For the time window, it has to be at least 1 min, other option is hour, day
     * @param sessionMap  are parameters on which formula will be computed
     * @return
     * @throws org.json.simple.parser.ParseException
     */
    public static String parseFormula(String formula, Map<String, Object>  sessionMap) throws ParseException {
        log.info("parsing formula " + formula);
        Set<String> set = RawDataParser.parseKeyArgs(formula);
        Map<String, String> map = new ConcurrentHashMap<String, String>();
        Map<String, Integer> addedToStack = new ConcurrentHashMap<String, Integer>();
        JSONObject jsonObject = new JSONObject(sessionMap);
        Set<String> addedToCalc= new HashSet<String>(); //adding only once per formula pass, to avoid double adding for the same key

        //first search for stats arguments like avg(node1.param1)
        for(String key : RawDataParser.parseKeyArgs(formula)){
            if(key.startsWith("avg") || key.startsWith("min") || key.startsWith("max") || key.startsWith("std")){
                log.info("applying stats calculation on "+key);
                String OPERATOR = key.substring(0, 3);
                String stringReplacement = "";
                int startIndex = key.indexOf(",") == -1? key.indexOf("(")+1 : key.lastIndexOf(",")+1;
                String realKey = key.substring(startIndex, key.indexOf(")")).trim();
                log.info("stats is on param "+ realKey);
                UtilStats simpleStats = statValues.get(realKey);

                if(key.indexOf(",") == -1){
                    if(simpleStats == null)
                        simpleStats = new UtilStats();
                    statValues.put(realKey, simpleStats);
                } else {
                    String []splits = key.split(",");
                    if(splits.length != 3) {
                        log.warn("length not of the correct size, use default stats");
                        if(simpleStats == null)
                            simpleStats = new UtilStats();
                        statValues.put(realKey, simpleStats);
                    }
                    else {
                        if("samples".equalsIgnoreCase(splits[1].trim())){
                            log.info("init stats calculation on the number of samples");
                            int index = Utils.getDouble(splits[0].substring(splits[0].indexOf("(")+1).trim()).intValue();
                            if(simpleStats == null)
                                simpleStats = new UtilStats(index);
                            statValues.put(realKey, simpleStats);
                            stringReplacement = key.substring(key.indexOf("(")+1, key.lastIndexOf(",") +1);
                        } else {
                            if("min".equalsIgnoreCase(splits[1].trim()) || "minutes".equalsIgnoreCase(splits[1].trim())){
                                int index = Utils.getDouble(splits[0].substring(splits[0].indexOf("(")+1).trim()).intValue();
                                //here I need windowtimer
                            } else if("hour".equalsIgnoreCase(splits[1].trim()) || "hour".equalsIgnoreCase(splits[1].trim())){
                                int index = Utils.getDouble(splits[0].substring(splits[0].indexOf("(")+1).trim()).intValue();
                                //here I need windowtimer
                            } else if("day".equalsIgnoreCase(splits[1].trim()) || "days".equalsIgnoreCase(splits[1].trim())){
                                int index = Utils.getDouble(splits[0].substring(splits[0].indexOf("(")+1).trim()).intValue();
                                //here I need windowtimer
                            } else
                                simpleStats = new UtilStats();
                        }
                    }
                }

                Object obj = RawDataParser.findObjForKey(realKey, jsonObject);

                if(obj!= null ){
                    if(!addedToCalc.contains(realKey)){
                        simpleStats.addSample(Utils.getDouble(obj.toString()));
                        addedToCalc.add(realKey);
                    }
                } else {
                    log.warn("stats for "  + realKey + " not found");
                }

                log.info("added for the line "+ key + ", with param "+ realKey + ", stats:"+ simpleStats.toString());
                //this is regexp need to get rid of ( chars
                formula = formula.replaceAll(OPERATOR+"\\(",OPERATOR);
                formula = formula.replaceAll("<"+OPERATOR + stringReplacement + realKey + "\\)>" , String.valueOf(simpleStats.getValueForOperator(OPERATOR)));
                formula = formula.replaceAll("<"+OPERATOR + stringReplacement +" " + realKey + "\\)>" , String.valueOf(simpleStats.getValueForOperator(OPERATOR)));
            }
        }

        for(String key: set){
            Object obj = RawDataParser.findObjForKey(key, jsonObject);
            if(obj != null){
                map.put(key, obj.toString());
            }
        }

        if(formula.indexOf(">[-") > -1){
            String [] pastValues = formula.split("]");
            for(String s : pastValues){
                String []nextPass = s.split("<");
                for(String sss : nextPass){
                    if(sss.indexOf(">[-") > -1){
                        String key = sss.substring(0, sss.indexOf(">")).trim();
                        if(map.get(key) == null){
                            throw new ParseException(1, "key" + key + " not in JSON object");
                        }
                        ArrayList stack;
                        if(prevValues.get(key) != null){
                            stack = prevValues.get(key);
                        }  else {
                            stack = new ArrayList();
                            prevValues.put(key, stack);
                        }

                        int stackNum = getStackNum(sss);

                        if(addedToStack.get(key) == null){
                            addedToStack.put(key, new Integer(1));
                            Double value = Double.parseDouble(map.get(key));
                            stack.add(value);
                            log.info("put in the stack ["+ key + "], value=" + value);
                        } else {
                            addedToStack.put(key, addedToStack.get(key) + 1);
                            log.info("skip in the stack since already added in this run ["+ key + "], value=" +
                                    map.get(key) );
                        }
                        String mapKey = key+"S"+stackNum;
                        if(stack.size() > stackNum)   {
                            Double prevD = (Double) stack.get(stack.size() - stackNum - 1);
                            log.info("put in the mapping from the stack: " + mapKey + "= " +  prevD);
                            map.put(mapKey, prevD.toString());
                        }
                        formula = formula.replace("<"+ key + ">" + "[-"+stackNum + "]", "<"+ mapKey + ">");
                    }
                }
            }
        }

        for(String key: addedToStack.keySet()){
            Integer count = addedToStack.get(key);
            ArrayList stack = prevValues.get(key);
            if(stack.size() > count){
                log.info("trim the stack [" + key + "], the size is " + stack.size() +
                        " and the max count by formula is " + count);
                for(int i = 0; i < stack.size() - count; i ++){
                    Object value = stack.remove(0);
                    log.info("trim key " + key + ", for value " + value);
                }
            }
        }


        //template keys can't have dots
        for(String key : map.keySet()) {
            formula = formula.replaceAll(key, key.replaceAll("\\.",""));
        }

        ST hello = new ST(formula);
        for(String key : map.keySet()) {
            hello.add(key.replaceAll("\\.",""), map.get(key));
        }
        String returnString =  hello.render();

        if(returnString.indexOf("distance") > -1){
            log.info("parse distance " + formula);
            try{
                String toReplace = returnString.substring(returnString.indexOf("distance"));
                toReplace = toReplace.substring(0, toReplace.indexOf(")")+1);
                String sub1 = toReplace.substring(toReplace.indexOf("distance")+8);
                sub1 = sub1.replaceAll("\\(","").replaceAll("\\)","");
                String [] split3 = sub1.split(",");
                String node1 = split3[0].trim();
                String node2 = split3[1].trim();
                JSONObject jsonObject1 = (JSONObject) (sessionMap.get(node1));
                JSONObject jsonObject2 = (JSONObject) (sessionMap.get(node2));
                jsonObject1 = (JSONObject) new JSONParser().parse(jsonObject1.get("rawData").toString());
                jsonObject2 = (JSONObject) new JSONParser().parse(jsonObject2.get("rawData").toString());
                log.info("parse distance first node=" + jsonObject1.toJSONString());
                log.info("parse distance second node=" + jsonObject1.toJSONString());
                Double latitude, longitude, latitude1, longitude1;
                latitude = jsonObject1.get("runtime_latitude") != null ? Utils.getDouble(jsonObject1.get("runtime_latitude")) :
                        Utils.getDouble(jsonObject1.get("latitude"));
                longitude = jsonObject1.get("runtime_longitude") != null ? Utils.getDouble(jsonObject1.get("runtime_longitude")) :
                        Utils.getDouble(jsonObject1.get("longitude"));
                latitude1 = jsonObject2.get("runtime_latitude") != null ? Utils.getDouble(jsonObject2.get("runtime_latitude")) :
                        Utils.getDouble(jsonObject2.get("latitude"));
                longitude1 = jsonObject2.get("runtime_longitude") != null ? Utils.getDouble(jsonObject2.get("runtime_longitude")) :
                        Utils.getDouble(jsonObject2.get("longitude"));
                double distance = FormulaParser.calculateDistance(latitude,longitude, latitude1, longitude1);
                log.info("Distance: " + distance);
                returnString = returnString.replace(toReplace, Double.toString(distance));
            } catch (Exception e){
                log.error(e.getLocalizedMessage());
            }
        }
        log.info("parsed formula = " + returnString);
        return returnString;
    }

    private static int getStackNum(String value) {
        return Integer.parseInt(value.substring(value.indexOf("[-")+2));
    }

    /**
     *  formula in format node1->param1~Text1 OPER node->param3~Text2.
     * @param nodeParams  are parameters on which formula will be computed
     * @return
     * @throws org.json.simple.parser.ParseException
     */
    public static int count(Map<String, Object>  nodeParams, String formula) throws ParseException {
        String returnString = formula.replaceAll("\\(", " \\( ").replaceAll("\\)", " \\) ").
                replaceAll("/", " / ");
        String [] split = returnString.split("\\s+");
        int count = 0;
        for(String s1 : split)   {
            String [] s2 = s1.split("->");
            if(s2.length == 2)  {
                try{
                    String node = s2[0];
                    String [] split2 = s2[1].split("~");
                    String value = split2[0];
                    String findString = split2[1];
                    JSONObject jsonObject = (JSONObject) (nodeParams.get(node));
                    Object rawValue =  ((JSONObject) new JSONParser().parse((String) jsonObject.get("rawData"))).get(value);
                    count += countString(rawValue.toString(), findString);
                } catch (Exception e){
                    log.error(e.getLocalizedMessage());
                }
            }
        }
        return count;
    }

    private static int countString(String str, String findStr){
        int lastIndex = 0;
        int count =0;
        while(lastIndex != -1){
            lastIndex = str.indexOf(findStr,lastIndex);
            if( lastIndex != -1){
                count ++;
                lastIndex+=findStr.length();
            }
        }
        return count;
    }

    public final static double AVERAGE_RADIUS_OF_EARTH = 6371;
    public static int calculateDistance(double userLat, double userLng, double venueLat, double venueLng) {
        log.info("calculateDistance("+userLat + ", "+ userLng + ", "+venueLat + ", "+ venueLng +")");

        double latDistance = Math.toRadians(userLat - venueLat);
        double lngDistance = Math.toRadians(userLng - venueLng);

        double a = (Math.sin(latDistance / 2) * Math.sin(latDistance / 2)) +
                (Math.cos(Math.toRadians(userLat))) *
                        (Math.cos(Math.toRadians(venueLat))) *
                        (Math.sin(lngDistance / 2)) *
                        (Math.sin(lngDistance / 2));

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));


        int round = (int) (Math.round(AVERAGE_RADIUS_OF_EARTH * c));
        log.info("calculateDistance=" + round);
        return round;

    }


}
