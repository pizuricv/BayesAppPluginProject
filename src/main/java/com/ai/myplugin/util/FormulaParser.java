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
    Map<String, UtilStats> statisticalSampleValues = new ConcurrentHashMap();
    Map<String, SlidingWindowStatsCounter> statisticalWindowValues = new ConcurrentHashMap();
    Map<String, UtilStats> counterValues = new ConcurrentHashMap();

    public void restStats(){
        prevValues.clear();
        statisticalSampleValues.clear();
        statisticalWindowValues.clear();
        counterValues.clear();
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
     *  For the count, you need to specify 4 parameters <count(key, number, samples/time, node.param> , where key can be a number or string
     *  and time can be expressed in minutes, hours or days
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
        Set<String> addedToCalcSlidingCounter= new HashSet<String>(); //adding only once per formula pass, to avoid double adding for the same key

        //first search for stats arguments like avg(node1.param1)
        for(String key : keySet){
            if(key.startsWith("avg") || key.startsWith("min") || key.startsWith("max") ||
                    key.startsWith("std") || key.startsWith("count")){
                log.info("applying stats calculation on "+key);

                String OPERATOR = key.substring(0, key.indexOf("("));
                String stringReplacement = "";
                int startIndex = key.indexOf(",") == -1? key.indexOf("(")+1 : key.lastIndexOf(",")+1;
                String realKey = key.substring(startIndex, key.indexOf(")")).trim();
                log.info("stats is on param "+ realKey);
                UtilStats sampleStats = statisticalSampleValues.get(realKey);
                UtilStats counterStats = counterValues.get(realKey);
                SlidingWindowStatsCounter slidingWindowStatsCounter = statisticalWindowValues.get(realKey);
                StatsType statsType = StatsType.STATS_COUNTER;

                //search term in case it is a count operator
                String searchTerm ="";

                //initialize counters
                if(key.indexOf(",") == -1){
                    if(sampleStats == null)   {
                        sampleStats = new UtilStats();
                        statisticalSampleValues.put(realKey, sampleStats);
                    }
                } else {
                    String []splits = key.split(",");

                    if(splits.length < 3){
                        log.error("length of the argument: " + key + ", not of the correct size");
                        throw new RuntimeException("length not of the correct size");
                    }
                    //to be replace in formula template
                    stringReplacement = key.substring(key.indexOf("(")+1, key.lastIndexOf(",") +1);
                    //<count(key, number, samples/time, node.param>
                    if(OPERATOR.equalsIgnoreCase("count")) {
                        searchTerm = splits[0].substring(splits[0].indexOf("(")+1).trim();
                        int index = Utils.getDouble(splits[1].trim()).intValue();
                        String howToMeasure = splits[2].trim();
                        if("samples".equalsIgnoreCase(howToMeasure)){
                            statsType = StatsType.COUNTER_SAMPLE;
                            if(counterStats == null)   {
                                counterStats = new UtilStats(index);
                                counterValues.put(realKey, counterStats);
                            }
                        } else{
                            statsType = StatsType.COUNTER_WINDOW;
                        }
                    }
                    else {
                        int index = Utils.getDouble(splits[0].substring(splits[0].indexOf("(")+1).trim()).intValue();
                        //<avg(5, samples, node1.param1)>, <avg(5, min, node1.param1)>
                        //to be replace in formula template
                        stringReplacement = key.substring(key.indexOf("(")+1, key.lastIndexOf(",") +1);
                        if("samples".equalsIgnoreCase(splits[1].trim())){
                            log.info("init stats calculation on the number of samples = "+index);
                            statsType = StatsType.STATS_COUNTER;
                            if(sampleStats == null){
                                sampleStats = new UtilStats(index);
                                statisticalSampleValues.put(realKey, sampleStats);
                            }
                        } else {
                            statsType = StatsType.STATS_WINDOW;
                            if("min".equalsIgnoreCase(splits[1].trim()) || "minutes".equalsIgnoreCase(splits[1].trim())){
                                //nothing to do index already of a good size
                            } else if("hour".equalsIgnoreCase(splits[1].trim()) || "hour".equalsIgnoreCase(splits[1].trim())){
                                index = index* 60;
                            } else if("day".equalsIgnoreCase(splits[1].trim()) || "days".equalsIgnoreCase(splits[1].trim())){
                                index = 24*60*index;
                            } else
                                throw new RuntimeException("key is wrong "+key);
                            log.info("init stats calculation on the time window = "+index+ "[minutes]");
                            if(slidingWindowStatsCounter == null) {
                                slidingWindowStatsCounter = new SlidingWindowStatsCounter(index, realKey);
                                statisticalWindowValues.put(realKey, slidingWindowStatsCounter);
                            }
                        }
                    }
                }

                Object obj = RawDataParser.findObjForKey(realKey, jsonObject);
                UtilStats tempStats = null;
                if(obj!= null ){
                    if(statsType.equals(StatsType.STATS_COUNTER)){
                        if(!addedToSampleCounter.contains(realKey)) {
                            sampleStats.addSample(Utils.getDouble(obj.toString()));
                            addedToSampleCounter.add(realKey);
                            log.info("added for the line "+ key + ", real key="+realKey);
                        }
                        tempStats = sampleStats;
                    } else if(statsType.equals(StatsType.STATS_WINDOW)){
                        if(!addedToCalcSlidingCounter.contains(realKey)){
                            slidingWindowStatsCounter.incrementAndGetStatsForCurrentMinute(Utils.getDouble(obj.toString()));
                            addedToCalcSlidingCounter.add(realKey);
                            log.info("added for the line "+ key + ", real key="+realKey);
                        }
                        tempStats = slidingWindowStatsCounter.getCurrentStats();
                    } else if(statsType.equals(StatsType.COUNTER_SAMPLE)){
                        if(!addedToCounter.contains(realKey)) {
                            try{
                                if(Utils.getDouble(obj.toString()).equals(Utils.getDouble(searchTerm)))
                                    counterStats.addSample(1);
                                else
                                    counterStats.addSample(0);
                            } catch (Exception e){
                                if(obj.toString().equals(searchTerm))
                                    counterStats.addSample(1);
                                else
                                    counterStats.addSample(0);
                            }
                            addedToSampleCounter.add(realKey);
                            log.info("added for the line "+ key + ", real key="+realKey);
                            }
                        tempStats = counterStats;
                    }
                    log.info("added for the line "+ key + ", with param "+ realKey + ", stats:"+ tempStats.toString());
                    //this is regexp need to get rid of ( chars
                    formula = formula.replaceAll(OPERATOR+"\\(",OPERATOR);
                    formula = formula.replaceAll("<"+OPERATOR + stringReplacement + realKey + "\\)>" , String.valueOf(tempStats.getValueForOperator(OPERATOR)));
                    formula = formula.replaceAll("<"+OPERATOR + stringReplacement +" " + realKey + "\\)>" , String.valueOf(tempStats.getValueForOperator(OPERATOR)));
                    log.info("formula after replacement for the key: "+key + " = "+formula);
                } else {
                    log.warn("stats for "  + realKey + " not found");
                }
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
        COUNTER_SAMPLE, COUNTER_WINDOW, STATS_COUNTER, STATS_WINDOW
    }

}
