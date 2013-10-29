package com.ai.myplugin.util;

import de.congrace.exp4j.Calculable;
import de.congrace.exp4j.ExpressionBuilder;
import de.congrace.exp4j.UnknownFunctionException;
import de.congrace.exp4j.UnparsableExpressionException;
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

    public static double executeFormula(String formula) throws Exception {
        System.out.println("execute formula " + formula);
        Calculable calc = new ExpressionBuilder(formula).build();
        double res = calc.calculate();
        System.out.println("result is " + res);
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
        String returnString = formula.replaceAll("\\(", " \\( ").replaceAll("\\)", " \\) ").
                replaceAll("/", " / ");
        String [] split = returnString.split("\\s+");
        Map<String, Double> map = new ConcurrentHashMap<String, Double>();
        for(String s1 : split)   {
            String [] s2 = s1.split("->");
            if(s2.length == 2)  {
                String node = s2[0];
                String value = s2[1];
                JSONObject jsonObject = (JSONObject) (nodeParams.get(node));
                Object rawValue =  ((JSONObject) new JSONParser().parse((String) jsonObject.get("rawData"))).get(value);
                map.put(s1, Utils.getDouble(rawValue));
            }
        }

        for(Map.Entry<String, Double> entry: map.entrySet()){
            returnString = returnString.replaceAll(entry.getKey() , entry.getValue().toString());
        }
        return returnString;
    };
}
