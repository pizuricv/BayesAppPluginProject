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
    Map<String, ArrayList> prevValues = new ConcurrentHashMap();
    Map<String, UtilStats> statisticalSampleValues;
    Map<String, SlidingWindowStatsCounter> statisticalWindowValues;
    Map<String, UtilStats> counterValues;
    Map<String, SlidingWindowStatsCounter> counterWindowValues;
    boolean initMaps = false;

    public synchronized void restStats(){
        if(initMaps){
            statisticalSampleValues = null;
            statisticalWindowValues = null;
            counterValues = null;
            counterWindowValues = null;
            initMaps = false;
        }
        prevValues.clear();
    }

    private synchronized void initStats(){
        statisticalSampleValues = new ConcurrentHashMap();
        statisticalWindowValues = new ConcurrentHashMap();
        counterValues = new ConcurrentHashMap();
        counterWindowValues = new ConcurrentHashMap();
        initMaps = true;
    }

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
     *  YOU CANT mix different aggregation types per raw key.
     *  Another option is to compare distance between two nodes, in that case it will take the latitude and longitude of these nodes
     *  like distance(node1,node2).
     *  For the count, you need to specify either 4 parameters <count(key, number, samples/time, node.param> , where key can be a number or string
     *  and time can be expressed in minutes, hours or days or 2 parameters <count(key, node.param> that will count all events
     * @param sessionMap  are parameters on which formula will be computed
     * @return
     * @throws org.json.simple.parser.ParseException
     */
    public String parseFormula(String formula, Map<String, Object>  sessionMap) throws ParseException {
        log.info("parsing formula " + formula);
        Set<String> keySet = RawDataParser.parseKeyArgs(formula);
        Map<String, String> map = new ConcurrentHashMap<String, String>();
        Map<String, Integer> addedToStack = new ConcurrentHashMap<String, Integer>();
        JSONObject jsonObject = new JSONObject(sessionMap);
        Set<String> addedToSampleCounter= new HashSet<String>(); //adding only once per formula pass, to avoid double adding for the same key
        Set<String> addedToCounter = new HashSet<String>(); //adding only once per formula pass, to avoid double adding for the same key
        Set<String> addedToSampleWindowCounter= new HashSet<String>(); //adding only once per formula pass, to avoid double adding for the same key
        Set<String> addedToCounterWindowCounter= new HashSet<String>(); //adding only once per formula pass, to avoid double adding for the same key

        //first search for stats arguments like avg(node1.param1)
        for(String key : keySet)
            if (key.startsWith("avg") || key.startsWith("min") || key.startsWith("max") ||
                    key.startsWith("std") || key.startsWith("count")) {
                log.info("applying stats calculation on " + key);
                if(!initMaps)
                    initStats();
                String OPERATOR = key.substring(0, key.indexOf("("));
                String stringReplacement = "";
                int startIndex = key.indexOf(",") == -1 ? key.indexOf("(") + 1 : key.lastIndexOf(",") + 1;
                String realKey = key.substring(startIndex, key.indexOf(")")).trim();
                log.info("stats is on param " + realKey);
                StatsType statsType = StatsType.STATS_REGULAR;

                //search term in case it is a count operator
                String searchTerm = "";

                //initialize counters
                if (key.indexOf(",") == -1) {
                    if (!OPERATOR.equalsIgnoreCase("count")) {
                        statsType = StatsType.STATS_REGULAR;
                        if (statisticalSampleValues.get(realKey) == null)
                            statisticalSampleValues.put(realKey, new UtilStats());
                    }
                }
                else {
                    //to be replace in formula template
                    stringReplacement = key.substring(key.indexOf("(") + 1, key.lastIndexOf(",") + 1);
                    String[] splits = key.split(",");
                    if(OPERATOR.equalsIgnoreCase("count")){
                        //<count(key,node.param>
                        if (splits.length == 2) {
                            searchTerm = splits[0].substring(splits[0].indexOf("(") + 1).trim();
                            statsType = StatsType.STATS_COUNTER;
                            if (counterValues.get(realKey) == null)
                                counterValues.put(realKey, new UtilStats());
                        }
                        //<count(key, number, samples/time, node.param>
                        else if (splits.length == 4) {
                            searchTerm = splits[0].substring(splits[0].indexOf("(") + 1).trim();
                            int index = Utils.getDouble(splits[1].trim()).intValue();
                            String howToMeasure = splits[2].trim();
                            if ("samples".equalsIgnoreCase(howToMeasure)) {
                                statsType = StatsType.STATS_COUNTER;
                                if (counterValues.get(realKey) == null)
                                    counterValues.put(realKey, new UtilStats(index));
                            } else {
                                statsType = StatsType.STATS_COUNTER_WINDOW;
                                index = index * getTimeInMinutesForString(splits[2].trim());
                                log.info("init stats calculation on the time window = " + index + "[minutes]");
                                if (counterWindowValues.get(realKey) == null)
                                    counterWindowValues.put(realKey, new SlidingWindowStatsCounter(index, realKey));
                            }
                        } else
                            throw new RuntimeException("formula for operator "+OPERATOR + " not correct");
                    } else {
                        int index = Utils.getDouble(splits[0].substring(splits[0].indexOf("(") + 1).trim()).intValue();
                        //<avg(5, samples, node1.param1)>, <avg(5, min, node1.param1)>
                        //to be replace in formula template
                        stringReplacement = key.substring(key.indexOf("(") + 1, key.lastIndexOf(",") + 1);
                        if ("samples".equalsIgnoreCase(splits[1].trim())) {
                            log.info("init stats calculation on the number of samples = " + index);
                            statsType = StatsType.STATS_REGULAR;
                            if (statisticalSampleValues.get(realKey) == null)
                                statisticalSampleValues.put(realKey, new UtilStats(index));
                        } else {
                            statsType = StatsType.STATS_REGULAR_WINDOW;
                            index = index * getTimeInMinutesForString(splits[1].trim());
                            log.info("init stats calculation on the time window = " + index + "[minutes]");
                            if (statisticalWindowValues.get(realKey) == null)
                                statisticalWindowValues.put(realKey, new SlidingWindowStatsCounter(index, realKey));
                        }
                    }
                }

                Object obj = RawDataParser.findObjForKey(realKey, jsonObject);
                UtilStats tempStats = null;
                //you should only add sample once in total, in case you call it several times for the same key and the same OPERATOR
                if (obj != null) {
                    if (statsType.equals(StatsType.STATS_REGULAR)) {
                        if (!addedToSampleCounter.contains(realKey)) {
                            statisticalSampleValues.get(realKey).addSample(Utils.getDouble(obj.toString()));
                            addedToSampleCounter.add(realKey);
                            log.info("added for the line " + key + ", real key=" + realKey);
                        }
                        tempStats = statisticalSampleValues.get(realKey);
                    } else if (statsType.equals(StatsType.STATS_REGULAR_WINDOW)) {
                        if (!addedToSampleWindowCounter.contains(realKey)) {
                            statisticalWindowValues.get(realKey).incrementAndGetStatsForCurrentMinute(Utils.getDouble(obj.toString()));
                            addedToSampleWindowCounter.add(realKey);
                            log.info("added for the line " + key + ", real key=" + realKey);
                        }
                        tempStats = statisticalWindowValues.get(realKey).getCurrentStats();
                    } else if (statsType.equals(StatsType.STATS_COUNTER)) {
                        if (!addedToCounter.contains(realKey)) {
                            counterValues.get(realKey).addSample(computeCounterValue(obj.toString(), searchTerm));
                            addedToSampleCounter.add(realKey);
                            log.info("added for the line " + key + ", real key=" + realKey);
                        }
                        tempStats = counterValues.get(realKey);
                    } else if (statsType.equals(StatsType.STATS_COUNTER_WINDOW)) {
                        if (!addedToCounterWindowCounter.contains(realKey)) {
                            counterWindowValues.get(realKey).incrementAndGetStatsForCurrentMinute(computeCounterValue(obj.toString(), searchTerm));
                            addedToCounterWindowCounter.add(realKey);
                            log.info("added for the line " + key + ", real key=" + realKey);
                        }
                        tempStats = counterWindowValues.get(realKey).getCurrentStats();
                    }
                    log.info("added for the line " + key + ", with param " + realKey + ", stats:" + tempStats.toString());
                    //this is regexp need to get rid of ( chars
                    formula = formula.replaceAll(OPERATOR + "\\(", OPERATOR);
                    formula = formula.replaceAll("<" + OPERATOR + stringReplacement + realKey + "\\)>", String.valueOf(tempStats.getValueForOperator(OPERATOR)));
                    formula = formula.replaceAll("<" + OPERATOR + stringReplacement + " " + realKey + "\\)>", String.valueOf(tempStats.getValueForOperator(OPERATOR)));
                    log.info("formula after replacement for the key: " + key + " = " + formula);
                } else {
                    log.warn("stats for " + realKey + " not found");
                }
            }

        for(String key: keySet){
            Object obj = RawDataParser.findObjForKey(key, jsonObject);
            if(obj != null){
                map.put(key, obj.toString());
            }
        }

        //check for the previous data required by the formula
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

        //location based formula distance(node1,node2)
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

    private double computeCounterValue(String obj, String searchTerm) {
        try {
            if (Utils.getDouble(obj).equals(Utils.getDouble(searchTerm)))
                return 1;
            else
                return 0;
        } catch (Exception e) {
            if (obj.equals(searchTerm))
                return 1;
            else if(searchTerm.endsWith("%") && obj.startsWith(searchTerm.substring(0, searchTerm.lastIndexOf("%"))))
                return 1;
            else
                return 0;
        }
    }

    private int getTimeInMinutesForString(String string) {
        log.debug("getTimeInMinutesForString " +string);
        if ("min".equalsIgnoreCase(string) || "minutes".equalsIgnoreCase(string)) {
            return 1;
        } else if ("hour".equalsIgnoreCase(string) || "hour".equalsIgnoreCase(string)) {
            return  60;
        } else if ("day".equalsIgnoreCase(string) || "days".equalsIgnoreCase(string)) {
            return  24 * 60 ;
        } else
            throw new RuntimeException("index is wrong " + string);
    }

    private static int getStackNum(String value) {
        return Integer.parseInt(value.substring(value.indexOf("[-")+2));
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

    private enum StatsType {
        STATS_COUNTER, STATS_COUNTER_WINDOW, STATS_REGULAR, STATS_REGULAR_WINDOW
    }

}
