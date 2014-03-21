package com.ai.myplugin.sensor;

import com.ai.bayes.plugins.BNSensorPlugin;
import com.ai.bayes.scenario.TestResult;
import com.ai.myplugin.util.APIKeys;
import com.ai.myplugin.util.EmptyTestResult;
import com.ai.myplugin.util.Rest;
import com.ai.util.resource.TestSessionContext;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import twitter4j.internal.org.json.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * Created by User: veselin
 * On Date: 21/03/14
 */
@PluginImplementation
public class SensulosSensor implements BNSensorPlugin{

    private static final Log log = LogFactory.getLog(SensulosSensor.class);
    String baseUrl = "http://in.sensolus.com:8080";
    String user = "apps4ghent";
    String [] states = {"Collected", "Not Collected"};
    private static final String NAME = "SensulosSensor";
    private static final String ID = "ID";
    private String id = "";


    @Override
    public TestResult execute(TestSessionContext testSessionContext) {
        //String url = baseUrl + "/server/rest/connectednodes?owner_id=" + user + "&token=" + APIKeys.getSensulosKey();
        String url = baseUrl + "/server/rest/connectednodes/" + id + "/data/lastvalues?token=" + APIKeys.getSensulosKey();
        try {
            String stringToParse = Rest.httpGet(url);
            log.info(stringToParse);
            final JSONObject jsonObject = new JSONObject(stringToParse);
            return new TestResult() {
                @Override
                public boolean isSuccess() {
                    return true;
                }

                @Override
                public String getName() {
                    return "Sensulos raw data";
                }

                @Override
                public String getObserverState() {
                    return states[0];
                }

                @Override
                public List<Map<String, Number>> getObserverStates() {
                    return null;
                }

                @Override
                public String getRawData() {
                    return jsonObject.toString();
                }
            };
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            return new EmptyTestResult();
        }
    }

    @Override
    public void shutdown(TestSessionContext testSessionContext) {

    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String[] getSupportedStates() {
        return states;
    }

    @Override
    public String[] getRequiredProperties() {
        return new String[]{ID};
    }

    @Override
    public String[] getRuntimeProperties() {
        return new String[]{};
    }

    @Override
    public void setProperty(String s, Object o) {
        if(ID.equalsIgnoreCase(s)){
            id = o.toString();
        }

    }

    @Override
    public Object getProperty(String s) {
        if(ID.equalsIgnoreCase(s)){
            return id;
        }
        throw new RuntimeException("property "+s + " not known by the sensor");
    }

    @Override
    public String getDescription() {
        return "Sensulos sensor";
    }

    public static void main(String []args) {
        SensulosSensor sensulosSensor = new SensulosSensor();
        sensulosSensor.setProperty(ID, "1071364b-83c4-4491-aad1-f35faaba1e63");
        TestSessionContext testSessionContext = new TestSessionContext(1);

        TestResult testResult = sensulosSensor.execute(testSessionContext);
        log.info(testResult.getRawData());
        log.info(testResult.getObserverState());

        sensulosSensor.setProperty(ID, "120ceb4a-92f2-4837-8ea4-4372cd559639");
        testResult = sensulosSensor.execute(testSessionContext);
        log.info(testResult.getRawData());
        log.info(testResult.getObserverState());

        sensulosSensor.setProperty(ID, "31a1967c-5a49-465c-a59d-7ef08f368b61");
        testResult = sensulosSensor.execute(testSessionContext);
        log.info(testResult.getRawData());
        log.info(testResult.getObserverState());


        sensulosSensor.setProperty(ID, "542bcd19-9ff2-4f84-a84d-7568d277ba5e");
        testResult = sensulosSensor.execute(testSessionContext);
        log.info(testResult.getRawData());
        log.info(testResult.getObserverState());


        sensulosSensor.setProperty(ID, "614ba218-913e-42a1-b4e8-af6fb232f2fb");

        testResult = sensulosSensor.execute(testSessionContext);
        log.info(testResult.getRawData());
        log.info(testResult.getObserverState());


        sensulosSensor.setProperty(ID, "8bb4ad28-fb61-492d-aaad-d0e544bf7f26");

        testResult = sensulosSensor.execute(testSessionContext);
        log.info(testResult.getRawData());
        log.info(testResult.getObserverState());

    }
}
