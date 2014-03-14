package com.ai.myplugin.sensor;

import com.ai.bayes.plugins.BNSensorPlugin;
import com.ai.bayes.scenario.TestResult;
import com.ai.myplugin.util.EmptyTestResult;
import com.ai.myplugin.util.Rest;
import com.ai.util.resource.TestSessionContext;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.List;
import java.util.Map;

/**
 * Created by User: veselin
 * On Date: 21/10/13
 */


@PluginImplementation
public class AirQualitySensor implements BNSensorPlugin{
    private static final Log log = LogFactory.getLog(AirQualitySensor.class);
    public static final String LOCATION = "location";
    String pathURL = "http://luchtkwaliteit.vmm.be/lijst.php";
    private String location = null;

    @Override
    public String[] getRequiredProperties() {
        return new String[]{LOCATION};
    }

    @Override
    public String[] getRuntimeProperties() {
        return new String[]{};
    }

    @Override
    public void setProperty(String s, Object o) {
        if(LOCATION.equals(s))
            location = o.toString();
        else
            throw new RuntimeException("Property "+ s + " not in the required settings");
    }

    @Override
    public Object getProperty(String s) {
        if(LOCATION.equals(s))
            return location;
        else
            throw new RuntimeException("Property "+ s + " not in the required settings");
    }

    @Override
    public String getDescription() {
        return "Air Quality";
    }

    @Override
    public TestResult execute(TestSessionContext testSessionContext) {
        log.info("execute "+ getName() + ", sensor type:" +this.getClass().getName());
        for(String property : getRequiredProperties()){
            if(getProperty(property) == null)
                throw new RuntimeException("Required property "+property + " not defined");
        }

        int value = -1;
        String stringToParse = "";

        try{
            stringToParse = Rest.httpGet(pathURL);
            Document doc = Jsoup.parse(stringToParse);
            for (Element table : doc.select("#stations")) {
                for (Element row : table.select("tr")) {
                    Elements tds = row.select("td");
                    if (tds.size() >4 && !"".equals(tds.get(4).text())) {
                        String location = tds.get(1).text();
                        if(location.equalsIgnoreCase((String) getProperty(LOCATION))) {
                            value = Integer.parseInt(tds.get(4).text().trim().replaceAll("      ",""));
                            break;
                        }
                        log.info("Found entry " + tds.get(0).text() + ":" + tds.get(1).text() + ":" +
                                tds.get(2).text() + ":" + tds.get(3).text() + ":" + tds.get(4).text());
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
            e.printStackTrace();
            return new EmptyTestResult();
        }
        if(value == -1){
            log.error("location not found");
            return new EmptyTestResult();
        }

        final int finalValue = value;
        return new TestResult() {
            @Override
            public boolean isSuccess() {
                return true;
            }

            @Override
            public String getName() {
                return "Water level result";
            }

            @Override
            public String getObserverState() {
                return mapValue(finalValue);
            }

            @Override
            public List<Map<String, Number>> getObserverStates() {
                return null;
            }

            @Override
            public String getRawData() {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("value", finalValue);
                return jsonObject.toJSONString();
            }
        };
    }

    @Override
    public void shutdown(TestSessionContext testSessionContext) {
        log.debug("Shutdown : " + getName() + ", sensor : "+this.getClass().getName());
    }

    private String mapValue(int finalValue) {
        if(finalValue < 3)
            return "Excellent";
        if(finalValue < 5)
            return "Good";
        if(finalValue < 7)
            return "Normal";
        if(finalValue < 9)
            return "Poor";
        return "Bad";
    }

    @Override
    public String getName() {
        return "AirQualitySensor";
    }

    @Override
    public String[] getSupportedStates() {
        return new String[] {"Excellent","Good", "Normal", "Poor", "Bad"};
    }

    public static void main(String []args){
        AirQualitySensor airQualitySensor = new AirQualitySensor();
        airQualitySensor.setProperty(LOCATION, "Gent");
        TestResult testResult = airQualitySensor.execute(null);
        log.info(testResult.getObserverState());
        log.info(testResult.getRawData());

        airQualitySensor.setProperty(LOCATION, "Antwerpen");
        testResult = airQualitySensor.execute(null);
        log.info(testResult.getObserverState());
        log.info(testResult.getRawData());
    }
}


