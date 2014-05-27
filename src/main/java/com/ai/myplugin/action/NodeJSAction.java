/**
 * Created by User: veselin
 * On Date: 18/03/14
 */

package com.ai.myplugin.action;

import com.ai.api.*;
import com.ai.myplugin.util.*;
import com.ai.myplugin.util.io.ExecResult;
import net.xeoh.plugins.base.annotations.PluginImplementation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.internal.org.json.JSONObject;
import org.stringtemplate.v4.ST;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@PluginImplementation
public class NodeJSAction implements ActuatorPlugin{
    private static final Logger log = LoggerFactory.getLogger(NodeJSAction.class);

    private static final String JAVA_SCRIPT = "javaScript";
    private String javaScriptCommand;
    private String nodePath = NodeConfig.getNodePath();
    private String workingDir = NodeConfig.getNodeDir();


    @Override
    public void action(SessionContext testSessionContext) {
        log.info("execute "+ getName() + ", action type:" +this.getClass().getName());

        if(testSessionContext != null && testSessionContext.getAttribute(SessionParams.RAW_DATA) != null){
            Map sessionMap = (Map) testSessionContext.getAttribute(SessionParams.RAW_DATA);
            JSONObject jsonObject = new JSONObject(sessionMap);
            javaScriptCommand = "RAW_STRING = '"+jsonObject.toString() + "';\n" + javaScriptCommand;
        }

        Node node = new Node(nodePath, workingDir);
        ExecResult result = node.executeScript(javaScriptCommand);
        // TODO do we want to fail on non-0 exit code?
    }

    @Override
    public String getName() {
        return "NodeJSAction";
    }

    @Override
    public Map<String,PropertyType> getRequiredProperties() {
        Map<String,PropertyType> map = new HashMap<>();
        if(getProperty(JAVA_SCRIPT) == null) {
            map.put(JAVA_SCRIPT, new PropertyType(DataType.STRING, true, true));
            return map;
        }
        Set<String> set = RawDataParser.parseKeyArgs((String) getProperty(JAVA_SCRIPT));
        Set<String> set2 = RawDataParser.getRuntimePropertiesFromTemplate((String) getProperty(JAVA_SCRIPT), "runtime_");
        set.removeAll(set2);
        set.add("javaScript");
        for(int i=0 ; i< set.size(); i++)
            map.put((String) set.toArray()[i], new PropertyType(DataType.STRING, true, true));
        return map;
    }

    @Override
    public void setProperty(String s, Object o) {
        if("javaScript".equals(s)){
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
        return "webscript action";
    }

}