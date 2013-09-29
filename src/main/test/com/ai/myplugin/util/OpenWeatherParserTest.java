package com.ai.myplugin.util;

import junit.framework.TestCase;

import java.util.List;
import java.util.Map;

/**
 * Created by User: veselin
 * On Date: 29/09/13
 */
public class OpenWeatherParserTest extends TestCase{

    public void testGetWeatherResultForWeekCodes() throws Exception {
        List list = OpenWeatherParser.getWeatherResultForWeekCodes("Gent");
        int i = 0;
        for(Object map: list){
            System.out.println("day " + i + " : "+ map);
            i++;
        }
    }

    public void testGetWeatherResultCodes() throws Exception {
        Map map = OpenWeatherParser.getWeatherResultCodes("Gent");
        System.out.println(map);


    }
}
