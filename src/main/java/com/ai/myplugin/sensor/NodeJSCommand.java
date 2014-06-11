/**
 * User: pizuricv
 * Date: 12/20/12
 */
package com.ai.myplugin.sensor;

import com.ai.api.*;
import com.ai.myplugin.util.*;
import com.ai.myplugin.util.io.ExecResult;
import net.xeoh.plugins.base.annotations.PluginImplementation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.stringtemplate.v4.ST;
import twitter4j.internal.org.json.JSONObject;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

// FIXME why does this depend on twitter4j internals?
@PluginImplementation
@PluginHeader(version = "1.0.1", author = "Veselin", category = "Java Script", iconURL = "http://app.waylay.io/icons/lab.png")
public class NodeJSCommand implements SensorPlugin {

    private static final Logger log = LoggerFactory.getLogger(NodeJSCommand.class);

    private static final String NAME = "JavaScript";
    private static final String JAVA_SCRIPT = "javaScript";

    private String javaScriptCommand;
    private String nodePath = NodeConfig.getNodePath();
    private String workingDir = NodeConfig.getNodeDir();



    @Override
    public Map<String, PropertyType> getRequiredProperties() {
        Map<String, PropertyType> map = new HashMap<>();
        if(getProperty("javaScript") == null)  {
            map.put(JAVA_SCRIPT, new PropertyType(DataType.STRING, true, true));
            return map;
        }
        Set<String> set = RawDataParser.parseKeyArgs((String) getProperty("javaScript"));
        Set<String> set2 = RawDataParser.getRuntimePropertiesFromTemplate((String) getProperty("javaScript"), "runtime_");
        set.removeAll(set2);
        set.add(JAVA_SCRIPT);
        for(int i=0 ; i< set.size(); i++)
            map.put((String) set.toArray()[i], new PropertyType());
        return map;
    }

    @Override
    public Map<String, PropertyType>  getRuntimeProperties() {
        Set<String> set = RawDataParser.getRuntimePropertiesFromTemplate((String) getProperty("javaScript"), "runtime_");
        if(set.size() == 0)
            return new HashMap<>();
        Map<String, PropertyType> map = new HashMap<>();
        for(int i=0 ; i< set.size(); i++)
            map.put((String) set.toArray()[i], new PropertyType());
        return map;
    }

    @Override
    public void setProperty(String s, Object o) {
        if(JAVA_SCRIPT.equals(s)){
            javaScriptCommand = o.toString();
        } else if ("nodePath".equals(s)){
            nodePath = o.toString();
        } else {
            Set<String> set = RawDataParser.parseKeyArgs((String) getProperty(JAVA_SCRIPT));
            if(set.contains(s)){
                String template = (String) getProperty(JAVA_SCRIPT);
                ST hello = new ST(template);
                try{
                    Utils.getDouble(o);
                } catch (Exception e){
                    o = "'" +o.toString() + "'";
                }
                hello.add(s, o);
                setProperty(JAVA_SCRIPT, hello.render());
            }
        }
    }

    @Override
    public Object getProperty(String s) {
        if("javaScript".endsWith(s)){
            return javaScriptCommand;
        }
        else{
            throw new RuntimeException("Property " + s + " not recognised by " + getName());
        }
    }

    @Override
    public String getDescription() {
        return "Node JS script, result needs to be a TestResult JSON string";
    }

    @Override
    public SensorResult execute(SessionContext testSessionContext) {
        log.info("execute "+ getName() + ", sensor type:" +this.getClass().getName());

        for(String runtimeProperty : getRuntimeProperties().keySet()){
            log.info("set property "+ runtimeProperty + ", for sensor " + getName());
            setProperty(runtimeProperty, testSessionContext.getAttribute(runtimeProperty));
        }
        if(testSessionContext != null && testSessionContext.getAttribute(SessionParams.RAW_DATA) != null){
            Map sessionMap = (Map) testSessionContext.getAttribute(SessionParams.RAW_DATA);
            JSONObject jsonObject = new JSONObject(sessionMap);
            javaScriptCommand = "RAW_STRING = '"+jsonObject.toString() + "';\n" + javaScriptCommand;
        }

        try {
            Node node = new Node(nodePath, workingDir);

            final ExecResult result = node.executeScript(javaScriptCommand);

            return new SensorResult() {
                @Override
                public boolean isSuccess() {
                    return  result.exitVal == 0 ;
                }

                @Override
                public String getName() {
                    return "node result";
                }

                @Override
                public String getObserverState() {
                    try {
                        JSONObject obj = new JSONObject(result.output);
                        return (String) obj.get("observedState");
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        return null;
                    }
                }

                @Override
                public List<Map<String, Number>> getObserverStates() {
                    try {
                        Map <String, Number> map = new ConcurrentHashMap<>();
                        List<Map <String, Number>> list = new ArrayList<>();
                        list.add(map);
                        JSONObject obj = new JSONObject(result.output);
                        JSONObject o  = (JSONObject) obj.get("observedStates");
                        Iterator iterator = o.keys();
                        while(iterator.hasNext()){
                            String state = (String) iterator.next();
                            Double value = Utils.getDouble(o.get(state));
                            map.put(state, value);
                        }
                        return list;
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        return null;
                    }
                }

                @Override
                public String getRawData() {
                    try {
                        JSONObject obj = new JSONObject(result.output);
                        return obj.get("rawData").toString();
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        return null;
                    }
                }
            }  ;

            // FIXME catching throwable is a bad idea
        } catch (Throwable t) {
            log.error(t.getLocalizedMessage(), t);
            return new EmptySensorResult();
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Set<String> getSupportedStates() {
        return new HashSet();
    }


    @Override
    public void setup(SessionContext testSessionContext) {
        log.debug("Setup : " + getName() + ", sensor : "+this.getClass().getName());
    }

    @Override
    public void shutdown(SessionContext testSessionContext) {
        log.debug("Shutdown : " + getName() + ", sensor : "+this.getClass().getName());
    }

    public static void main(String [] args) {
        NodeJSCommand nodeJSCommand = new NodeJSCommand();
        nodeJSCommand.getRequiredProperties();
        String javaScript =  "a = { observedState:\"world\",\n" +
                "      observedStates: {\n" +
                "        state1 : 0.5,\n" +
                "        state2 : 0.5\n" +
                "      },\n" +
                "      rawData: {\n" +
                "       data1: 2,\n" +
                "       data2: \"hello\"\n" +
                "     }\n" +
                "}\n" +
                "\n" +
                "console.log(a)" ;
        nodeJSCommand.setProperty("javaScript", javaScript);
        System.out.println(nodeJSCommand.getRequiredProperties().keySet().toString());

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
        log.info(testResult.toString());
        log.info("state " + testResult.getObserverState());
        log.info("rawData " + testResult.getRawData());
        //log.info("states " + testResult.getObserverStates());
    }
}
