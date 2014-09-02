/**
 * User: pizuricv
 * Date: 12/20/12
 */
package com.ai.myplugin.sensor;

import com.ai.api.*;
import com.ai.myplugin.util.*;
import com.ai.myplugin.util.io.ExecResult;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.xeoh.plugins.base.annotations.PluginImplementation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.stringtemplate.v4.ST;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

// Deprecated
//@PluginImplementation
//@PluginHeader(version = "1.0.1", author = "Veselin", category = "Java Script", iconURL = "http://app.waylay.io/icons/lab.png")
public class NodeJSCommand implements SensorPlugin {

    private static final Logger log = LoggerFactory.getLogger(NodeJSCommand.class);

    private static final String NAME = "JavaScript";
    private static final String JAVA_SCRIPT = "javaScript";
    public static final String NODE_PATH = "nodePath";

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
    public Map<String, RawDataType> getRequiredRawData() {
        Set<String> set = RawDataParser.getRuntimePropertiesFromTemplate((String) getProperty("javaScript"), "runtime_");
        if(set.size() == 0)
            return new HashMap<>();
        Map<String, RawDataType> map = new HashMap<>();
        for(int i=0 ; i< set.size(); i++)
            map.put((String) set.toArray()[i], new RawDataType("", DataType.STRING));
        return map;
    }

    @Override
    public void setProperty(String s, Object o) {
        switch (s) {
            case JAVA_SCRIPT:
                javaScriptCommand = o.toString();
                break;
            case NODE_PATH:
                nodePath = o.toString();
                break;
            default:
                Set<String> set = RawDataParser.parseKeyArgs((String) getProperty(JAVA_SCRIPT));
                if (set.contains(s)) {
                    String template = (String) getProperty(JAVA_SCRIPT);
                    ST hello = new ST(template);
                    try {
                        Utils.getDouble(o);
                    } catch (Exception e) {
                        o = "'" + o.toString() + "'";
                    }
                    hello.add(s, o);
                    setProperty(JAVA_SCRIPT, hello.render());
                }
                break;
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

        for(String runtimeProperty : getRequiredRawData().keySet()){
            log.info("set property "+ runtimeProperty + ", for sensor " + getName());
            setProperty(runtimeProperty, testSessionContext.getAttribute(runtimeProperty));
        }
        if(testSessionContext != null && testSessionContext.getAttribute(SessionParams.RAW_DATA) != null){
            Map sessionMap = (Map) testSessionContext.getAttribute(SessionParams.RAW_DATA);
            Gson gson = new GsonBuilder().create();
            String sessionMapJson = gson.toJson(sessionMap);
            // TODO why the raw string and not a RAW object?
            javaScriptCommand = "RAW_STRING = '" + sessionMapJson + "';\n" + javaScriptCommand;
        }

        try {
            Node node = new Node(nodePath, workingDir);

            final ExecResult result = node.executeScript(javaScriptCommand);

            final Gson gson = new GsonBuilder().create();
            Optional<JsonObject> output = Optional.ofNullable(gson.fromJson(result.output, JsonObject.class));

            return new SensorResult() {
                @Override
                public boolean isSuccess() {
                    return  result.exitVal == 0 ;
                }

                @Override
                public String getObserverState() {
                    return output
                            .flatMap(o -> Optional.ofNullable(o.get("observedState")))
                            .map(JsonElement::getAsString)
                            .orElse(null);
                }

                @Override
                public List<Map<String, Number>> getObserverStates() {
                    return output
                            .flatMap(o -> Optional.ofNullable(o.getAsJsonObject("observedStates")))
                            .map(observedStates -> {
                                Map <String, Number> observedStatesMap = new ConcurrentHashMap<>();
                                for(Map.Entry<String, JsonElement> entry:observedStates.entrySet()){
                                    observedStatesMap.put(entry.getKey(), entry.getValue().getAsNumber());
                                }
                                List<Map <String, Number>> list = new ArrayList<>();
                                list.add(observedStatesMap);
                                return list;
                            })
                            .orElse(null);
                }

                @Override
                public String getRawData() {
                   return output
                           .flatMap(o -> Optional.ofNullable(o.get("rawData")))
                           .map(rawData -> gson.toJson(rawData))
                           .orElse(null);
                }
            };

            // FIXME catching throwable is a bad idea
        } catch (Throwable t) {
            log.error(t.getLocalizedMessage(), t);
            return SensorResultBuilder.failure(t.getMessage()).build();
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Set<String> getSupportedStates() {
        return new HashSet<>();
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
                "    for(location in locations.rawData.location){\n" +
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
