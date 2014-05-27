/**
 * Created by User: veselin
 * On Date: 22/02/14
 */

package com.ai.myplugin.sensor;
import com.ai.api.SensorResult;
import com.ai.api.SessionContext;
import com.ai.api.SessionParams;
import junit.framework.TestCase;
import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class NodeJSCommandTest extends TestCase{

    public void testExecute(){
        NodeJSCommand nodeJSCommand = new NodeJSCommand();
        String javaScript =  "a = { observedState:\"world\",\n" +
                "      observedStates: {\n" +
                "        state1 : 0.5,\n" +
                "        state2 : 0.5\n" +
                "      },\n" +
                "      rawData: {\n" +
                "       data1: 2,\n" +
                "       data2: \"hello\"\n" +
                "     }\n" +
                "};\n" +
                "\n" +
                "console.log(a);" ;
        nodeJSCommand.setProperty("javaScript", javaScript);

        // TestResult testResult = nodeJSCommand.execute(null);
        // log.info(testResult.toString());
        // log.info("state " + testResult.getObserverState());
        // log.info("rawData " + testResult.getRawData());
        // log.info("states " + testResult.getObserverStates());

        javaScript = "var request = require(\"request\");\n" +
                "var url = \"http://datatank.gent.be/Onderwijs&Opvoeding/Basisscholen.json\";\n" +
                "request({\n" +
                "    url: url,\n" +
                "    json: true\n" +
                "}, function (error, response, body) {\n" +
                "\n" +
                " if (!error && response.statusCode === 200) {\n" +
                "    var locations = {\n" +
                "       observedState: \"Found\",\n" +
                "       rawData : {\n" +
                "         locations: body.Basisscholen\n" +
                "       }\n" +
                "    };\n" +
                "    for(location in locations.rawData.locations){\n" +
                "      locations.rawData.locations[location].longitude = locations.rawData.locations[location].long;\n" +
                "      locations.rawData.locations[location].latitude = locations.rawData.locations[location].lat;\n" +
                "   }\n" +
                "    console.log(JSON.stringify(locations));\n" +
                "  }\n" +
                "});";
        nodeJSCommand.setProperty("javaScript", javaScript);

        SensorResult testResult = nodeJSCommand.execute(null);
        System.out.println(testResult.toString());
        System.out.println("state " + testResult.getObserverState());
        System.out.println("rawData " + testResult.getRawData());
    }

    public void testRuntimeTemplate(){
        NodeJSCommand nodeJSCommand = new NodeJSCommand();
        assertNotNull(nodeJSCommand.getRequiredProperties().get("javaScript"));
        String javaScript =  "a = { observedState:\"world\",\n" +
                "      observedStates: {\n" +
                "        state1 : 0.5,\n" +
                "        state2 : 0.5\n" +
                "      },\n" +
                "      rawData: {\n" +
                "       data1: 2,\n" +
                "       data2: " + "<runtime_hello>" +"\n" +
                "     }\n" +
                "};\n" +
                "\n" +
                "console.log(a);" ;
        nodeJSCommand.setProperty("javaScript", javaScript);
        assertTrue(nodeJSCommand.getRuntimeProperties().keySet().contains("runtime_hello"));
        assertEquals(1, nodeJSCommand.getRequiredProperties().size());
        SessionContext testSessionContext = new SessionContext(1);
        testSessionContext.setAttribute("runtime_hello", "5");

        SensorResult testResult = nodeJSCommand.execute(testSessionContext);
        assertTrue(testResult.getRawData().contains("5"));



        nodeJSCommand = new NodeJSCommand();
        javaScript =  "a = { observedState:\"world\",\n" +
                "      observedStates: {\n" +
                "        state1 : 0.5,\n" +
                "        state2 : 0.5\n" +
                "      },\n" +
                "      rawData: {\n" +
                "       data1: 2,\n" +
                "       data2: " + "<runtime_hello>" +"\n" +
                "     }\n" +
                "};\n" +
                "\n" +
                "console.log(a);" ;
        nodeJSCommand.setProperty("javaScript", javaScript);

        testSessionContext.setAttribute("runtime_hello", "hello");

        testResult = nodeJSCommand.execute(testSessionContext);
        assertTrue(testResult.getRawData().contains("hello"));

    }


    public void testGlobalRAWData(){
        NodeJSCommand nodeJSCommand = new NodeJSCommand();
        String javaScript =  "b = JSON.parse(RAW_STRING);\n" +
                "a = { observedState:\"world\",\n" +
                "      observedStates: {\n" +
                "        state1 : 0.5,\n" +
                "        state2 : 0.5\n" +
                "      },\n" +
                "      rawData: b.node1.rawData\n"+
                "};\n" +
                "\n" +
                "console.log(a);" ;
        nodeJSCommand.setProperty("javaScript", javaScript);

        JSONObject jsonObject = new JSONObject();
        JSONObject raw = new JSONObject();
        raw.put("key", "value");

        jsonObject.put("rawData", raw);
        Map<String, Object> mapTestResult = new HashMap<String, Object>();
        mapTestResult.put("node1", jsonObject);

        SessionContext testSessionContext = new SessionContext(2);

        testSessionContext.setAttribute(SessionParams.RAW_DATA, mapTestResult);
        SensorResult testResult = nodeJSCommand.execute(testSessionContext);
        assertTrue(nodeJSCommand.getProperty("javaScript").toString().contains("RAW_STRING"));
        assertEquals(raw.toJSONString(), testResult.getRawData());

    }


}
