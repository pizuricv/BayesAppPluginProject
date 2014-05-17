/**
 * Created by User: veselin
 * On Date: 29/09/13
 */

package com.ai.myplugin.util;

import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.util.List;
import java.util.Map;

public class OpenWeatherParserTest extends TestCase{
    private static final Log log = LogFactory.getLog(OpenWeatherParserTest.class);

    public void testGetWeatherResultForWeekCodes() throws Exception {
        List list = OpenWeatherParser.getWeatherResultForWeekCodes("Gent");
        int i = 0;
        for(Object map: list){
            log.debug("day " + i + " : "+ map);
            i++;
        }
    }

    public void testGetWeatherResultCodes() throws Exception {
        Map map = OpenWeatherParser.getWeatherResultCodes("Gent");
        log.debug(map);


    }
}
