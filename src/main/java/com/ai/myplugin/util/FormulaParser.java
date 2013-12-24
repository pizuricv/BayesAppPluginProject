package com.ai.myplugin.util;

import de.congrace.exp4j.Calculable;
import de.congrace.exp4j.ExpressionBuilder;
import de.congrace.exp4j.UnknownFunctionException;
import de.congrace.exp4j.UnparsableExpressionException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by User: veselin
 * On Date: 28/10/13
 */
public class FormulaParser {
    private static final Log log = LogFactory.getLog(FormulaParser.class);

    public static double executeFormula(String formula) throws Exception {
        log.debug("execute formula " + formula);
        Calculable calc = new ExpressionBuilder(formula).build();
        double res = calc.calculate();
        log.debug("result is " + res);
        return res;
    }

    /**
     *  formula in format node1->param1 OPER node->param3 OPER node->param3 .
     *  if formula includes delta: delta(node1->param1) means delta from previous collection
     *  dt means delta in time from previous collection
     * @param nodeParams  are parameters on which formula will be computed
     * @return
     * @throws org.json.simple.parser.ParseException
     */
    public static String parse(Map<String, Object>  nodeParams, String formula) throws ParseException {
        log.debug("parse formula " + formula);
        String returnString = formula.replaceAll("\\(", " \\( ").replaceAll("\\)", " \\) ").
                replaceAll("/", " / ");
        String [] split = returnString.split("\\s+");
        Map<String, Double> map = new ConcurrentHashMap<String, Double>();
        for(String s1 : split)   {
            String [] s2 = s1.split("->");
            if(s2.length == 2)  {
                try{
                    String node = s2[0];
                    String value = s2[1];
                    JSONObject jsonObject = (JSONObject) (nodeParams.get(node));
                    Object rawValue =  ((JSONObject) new JSONParser().parse((String) jsonObject.get("rawData"))).get(value);
                    map.put(s1, Utils.getDouble(rawValue));
                }
                catch (Exception e){
                    log.error(e.getLocalizedMessage());
                }
            }
        }

        for(Map.Entry<String, Double> entry: map.entrySet()){
            returnString = returnString.replaceAll(entry.getKey() , entry.getValue().toString());
        }
        if(returnString.indexOf("distance") > -1){
            log.debug("parse distance " + formula);
            try{
                String toReplace = returnString.substring(returnString.indexOf("distance"));
                toReplace = toReplace.substring(0, toReplace.indexOf(")")+1);
                String sub1 = toReplace.substring(toReplace.indexOf("distance")+8);
                sub1 = sub1.replaceAll("\\(","").replaceAll("\\)","");
                String [] split3 = sub1.split(",");
                String node1 = split3[0].trim();
                String node2 = split3[1].trim();
                JSONObject jsonObject1 = (JSONObject) (nodeParams.get(node1));
                JSONObject jsonObject2 = (JSONObject) (nodeParams.get(node2));
                jsonObject1 = (JSONObject) new JSONParser().parse(jsonObject1.get("rawData").toString());
                jsonObject2 = (JSONObject) new JSONParser().parse(jsonObject2.get("rawData").toString());
                double distance = FormulaParser.calculateDistance(Utils.getDouble(jsonObject1.get("latitude")),
                        Utils.getDouble(jsonObject1.get("longitude")), Utils.getDouble(jsonObject2.get("latitude")),
                        Utils.getDouble(jsonObject2.get("longitude")));
                log.debug("Distance: "+ distance);
                returnString = returnString.replace(toReplace, Double.toString(distance));
            } catch (Exception e){
                log.error(e.getLocalizedMessage());
            }
        }
        log.debug("returnString " + returnString);
        return returnString;
    };

    /**
     *  formula in format node1->param1~Text1 OPER node->param3~Text2.
     *  if formula includes delta: delta(node1->param1) means delta from previous collection
     *  dt means delta in time from previous collection
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

        double latDistance = Math.toRadians(userLat - venueLat);
        double lngDistance = Math.toRadians(userLng - venueLng);

        double a = (Math.sin(latDistance / 2) * Math.sin(latDistance / 2)) +
                (Math.cos(Math.toRadians(userLat))) *
                        (Math.cos(Math.toRadians(venueLat))) *
                        (Math.sin(lngDistance / 2)) *
                        (Math.sin(lngDistance / 2));

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return (int) (Math.round(AVERAGE_RADIUS_OF_EARTH * c));

    }
}
