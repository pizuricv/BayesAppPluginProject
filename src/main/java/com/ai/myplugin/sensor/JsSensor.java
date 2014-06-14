/**
 * User: pizuricv
 * Date: 12/20/12
 */
package com.ai.myplugin.sensor;

import com.ai.api.*;
import com.ai.myplugin.util.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.stringtemplate.v4.ST;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

// FIXME why does this depend on twitter4j internals?
//TODO add plugin annotations
public class JsSensor implements SensorPlugin {

    private static final Logger log = LoggerFactory.getLogger(NodeJSCommand.class);

    private static final String NAME = "JavaScript";
    private static final String JAVA_SCRIPT = "javaScript";
    //TODO add port number and server address

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
        switch (s) {
            case JAVA_SCRIPT:
                javaScriptCommand = o.toString();
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

        for(String runtimeProperty : getRuntimeProperties().keySet()){
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
            //TODO change JSON part ....with latest libraries
            javaScriptCommand = Base64.getEncoder().encodeToString(javaScriptCommand.getBytes()).toString();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("jsonrpc", "2.0");
            jsonObject.put("method", "waylay_rpc");
            jsonObject.put("params", Arrays.asList(javaScriptCommand));
            jsonObject.put("id", 2);
            javaScriptCommand = jsonObject.toJSONString();

            Rest.RestReponse result = Rest.httpPost("http://localhost:5080", javaScriptCommand, "UTF-8", "application/json");
            Optional<JSONObject> output = Optional.ofNullable(result.json());

            return new SensorResult() {
                @Override
                public boolean isSuccess() {
                    return  true;
                }

                @Override
                public String getName() {
                    return "node result";
                }

                @Override
                public String getObserverState() {
                    return ((JSONObject)output.get().get("result")).get("observedState").toString();
                }

                @Override
                public List<Map<String, Number>> getObserverStates() {
                    return null;
                }

                @Override
                public String getRawData() {
                    return ((JSONObject)output.get().get("result")).get("rawData").toString();
                }
            };

            // FIXME catching throwable is a bad idea
        } catch (Throwable t) {
            log.error(t.getLocalizedMessage(), t);
            return SensorResultBuilder.failure().build();
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
        JsSensor nodeJSCommand = new JsSensor();
        nodeJSCommand.getRequiredProperties();
        String javaScript =  "ret = { observedState: \"hello\", rawData: \"hello2\"}";
        nodeJSCommand.setProperty("javaScript", javaScript);
        SensorResult testResult = nodeJSCommand.execute(null);
        System.out.println(testResult.toString());
        System.out.println("state " + testResult.getObserverState());
        System.out.println("rawData " + testResult.getRawData());

    }
}
