/**
 * Created by User: veselin
 * On Date: 07/11/13
 */
package com.ai.myplugin.action;

import com.ai.bayes.plugins.BNActionPlugin;
import com.ai.bayes.scenario.ActionResult;
import com.ai.util.resource.NodeSessionParams;
import com.ai.util.resource.TestSessionContext;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Map;

@PluginImplementation
public class WebHookAction implements BNActionPlugin{
    private static String HOOK_URL = "callback_URL";
    private static final Log log = LogFactory.getLog(WebHookAction.class);
    private URL hook;

    @Override
    public String[] getRequiredProperties() {
        return new String[] {HOOK_URL};
    }

    @Override
    public void setProperty(String string, Object o) {
        if(string.equalsIgnoreCase(HOOK_URL)) {
            try {
                hook = new URL(o.toString());
            } catch (MalformedURLException e) {
                e.printStackTrace();
                log.error(e.getLocalizedMessage());
            }} else {
            throw new RuntimeException("Property "+ string + " not in the required settings");
        }
    }

    @Override
    public Object getProperty(String s) {
        if(s.equalsIgnoreCase(HOOK_URL))
            return hook;
        return null;
    }

    @Override
    public String getDescription() {
        return "Web Hook Action";
    }

    @Override
    public ActionResult action(TestSessionContext testSessionContext) {
        boolean testResult = false;
        if(hook == null){
            throw new RuntimeException("URL post hook not defined");
        }
        Map map = (Map) testSessionContext.getAttribute(NodeSessionParams.RAW_DATA);
        Long id = testSessionContext.getId();
        String actionNode = (String) testSessionContext.getAttribute(NodeSessionParams.ACTION_NODE);
        String targetState = (String) testSessionContext.getAttribute(NodeSessionParams.TARGET_STATE);
        String target = (String) testSessionContext.getAttribute(NodeSessionParams.TARGET_NODE);
        String node = (String) testSessionContext.getAttribute(NodeSessionParams.NODE_NAME);
        String nodeState = (String) testSessionContext.getAttribute(NodeSessionParams.NODE_TRIGGERED_STATE);
        String resource = (String) testSessionContext.getAttribute(NodeSessionParams.RESOURCE);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("rawData", map);
        jsonObject.put("actionNode", actionNode);
        jsonObject.put("targetState", targetState);
        jsonObject.put("target", target);
        jsonObject.put("node", node);
        jsonObject.put("nodeState", nodeState);
        jsonObject.put("resource", resource);
        jsonObject.put("id", id);

        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) hook.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
            log.error(e.getLocalizedMessage());
        }
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setInstanceFollowRedirects(false);
        try {
            connection.setRequestMethod("POST");
        } catch (ProtocolException e) {
            e.printStackTrace();
            log.error(e.getLocalizedMessage());
        }
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestProperty("charset", "utf-8");
        connection.setRequestProperty("Content-Length", "" + Integer.toString(jsonObject.toJSONString().getBytes().length));
        connection.setUseCaches (false);

        DataOutputStream wr = null;
        try {
            wr = new DataOutputStream(connection.getOutputStream ());
            wr.writeBytes(jsonObject.toJSONString());
            wr.flush();
            wr.close();
            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
            log.error(e.getLocalizedMessage());
        }
        testResult = true;
        final boolean finalTestResult = testResult;
        return new ActionResult() {
            @Override
            public boolean isSuccess() {
                return finalTestResult;
            }

            @Override
            public String getObserverState() {
                return null;
            }
        };
    }

    @Override
    public String getName() {
        return "WebHookAction";
    }
}