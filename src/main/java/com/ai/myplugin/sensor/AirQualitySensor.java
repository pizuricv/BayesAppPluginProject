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

import java.util.List;
import java.util.Map;

/**
 * Created by User: veselin
 * On Date: 21/10/13
 */

/*
http://luchtkwaliteit.vmm.be/lijst.php
</tr>
            <tr class="trEVEN">
    <td headers="Details">
        <a href="details.php?station=44R701" title="Gent-Baudelostraat">
            <img src="image/information.png" />
        </a>
    </td>
    <td headers="Gemeente">
        <a href="details.php?station=44R701" title="Gent-Baudelostraat">
            Gent
        </a>
    </td>
    <td headers="Locatie">
        <a href="details.php?station=44R701" title="Gent-Baudelostraat">
            Baudelostraat
        </a>
    </td>
    <td headers="Provincie">
        <a href="details.php?station=44R701" title="Gent-Baudelostraat">
            Oost-Vlaanderen
        </a>
    </td>
    <td headers="Index" style="text-align:left;">
        <a href="details.php?station=44R701" title="Gent-Baudelostraat">
            <span>&nbsp;<span class="index3">&nbsp;&nbsp;&nbsp;</span>
                &nbsp;3</span>
                                </a>
    </td>
</tr>
 */

@PluginImplementation
public class AirQualitySensor implements BNSensorPlugin{
    private static final Log log = LogFactory.getLog(AirQualitySensor.class);
    public static final String LOCATION = "location";
    private String location = null;

    @Override
    public String[] getRequiredProperties() {
        return new String[]{LOCATION};
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

        boolean testSuccess = true;
        int value = -1;
        String stringToParse = "";

        String pathURL = "http://luchtkwaliteit.vmm.be/lijst.php";
        try{
            stringToParse = Rest.httpGet(pathURL);
            //log.debug(stringToParse);
        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
            testSuccess = false;
        }
        if(testSuccess){
            testSuccess = false;
            int len = stringToParse.indexOf(location);
            try{
                if(len > 0){
                    stringToParse = stringToParse.substring(len);
                    len = stringToParse.indexOf("Index");
                    if(len > 0) {
                        stringToParse = stringToParse.substring(len);
                        len = stringToParse.indexOf("\t&nbsp;");
                        if(len > 0){
                            stringToParse = stringToParse.substring(len);
                            stringToParse = stringToParse.substring(stringToParse.indexOf("&nbsp;"),
                                    stringToParse.indexOf("</s")).replaceAll("&nbsp;","").trim();
                            value = Integer.parseInt(stringToParse);
                            testSuccess = true;
                        }
                    }
                }
            }catch (Exception e){
                log.error(e.getLocalizedMessage());
                testSuccess = false;
            }
        }
        if(testSuccess)  {
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
        else return new EmptyTestResult();
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
        log.debug(testResult.getObserverState());
        log.debug(testResult.getRawData());

        airQualitySensor.setProperty(LOCATION, "Antwerp");
        testResult = airQualitySensor.execute(null);
        log.debug(testResult.getObserverState());
        log.debug(testResult.getRawData());
    }
}


