package com.ai.myplugin.sensor;

import com.ai.bayes.plugins.BNSensorPlugin;
import com.ai.bayes.scenario.TestResult;
import com.ai.myplugin.util.*;
import com.ai.util.resource.TestSessionContext;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by User: veselin
 * On Date: 26/12/13
 */
@PluginImplementation
public class TreeSensor implements BNSensorPlugin{
    protected static final Log log = LogFactory.getLog(TreeSensor.class);
    static final String DISTANCE = "distance";
    static final String CITY = "city";
    static final String SHOW_ALL = "showAll";
    static final String RUNTIME_LATITUDE = "runtime_latitude";
    static final String RUNTIME_LONGITUDE = "runtime_longitude";
    String pathURL = "http://datatank.gent.be/MilieuEnNatuur/Bomeninventaris.json";
    boolean showAll = false;

    Map<String, Object> propertiesMap = new ConcurrentHashMap<String, Object>();

    String [] states = {"Found", "Not Found"};
    private static final String NAME = "TreeSensor";

    @Override
    public String[] getRequiredProperties() {
        return new String[]{DISTANCE, CITY, SHOW_ALL};
    }

    @Override
    public String[] getRuntimeProperties() {
        return new String[]{RUNTIME_LATITUDE, RUNTIME_LONGITUDE};
    }

    @Override
    public void setProperty(String string, Object obj) {
        if(Arrays.asList(getRequiredProperties()).contains(string)) {
            propertiesMap.put(string, obj);
        } else {
            throw new RuntimeException("Property "+ string + " not in the required settings");
        }
    }

    @Override
    public Object getProperty(String string) {
        return propertiesMap.get(string);
    }

    @Override
    public String getDescription() {
        return "Check for parking space is within a distance from a given location";
    }

    @Override
    public TestResult execute(TestSessionContext testSessionContext) {
        log.info("execute "+ getName() + ", sensor type:" +this.getClass().getName());
        if(getProperty(DISTANCE) == null)
            throw new RuntimeException("distance not set");
        if(getProperty(CITY) == null)
            throw new RuntimeException("city not set");
        if(!getProperty(CITY).toString().equalsIgnoreCase("Gent"))
            throw new RuntimeException("only Gent supported for now");
        if(getProperty(SHOW_ALL) != null){
            showAll = Boolean.parseBoolean((String) getProperty(SHOW_ALL));
        }

        Object rt1 = testSessionContext.getAttribute(RUNTIME_LATITUDE);
        Object rt2 = testSessionContext.getAttribute(RUNTIME_LONGITUDE);
        if(rt1 == null || rt2 == null){
            log.warn("no runtime longitude or latitude given");
            return new EmptyTestResult();
        }
        Double runtime_latitude = Utils.getDouble(rt1);
        Double runtime_longitude = Utils.getDouble(rt2);
        log.info("Current location: "+ runtime_latitude + ","+runtime_longitude);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(RUNTIME_LATITUDE, runtime_latitude);
        jsonObject.put(RUNTIME_LONGITUDE, runtime_longitude);

        ArrayList<MyTreeData> treeDatas = new ArrayList<MyTreeData>();
        try{
            String stringToParse = Rest.httpGet(pathURL);
            log.debug(stringToParse);
            JSONObject jsonObj = (JSONObject) new JSONParser().parse(stringToParse);
            JSONArray trees = (JSONArray)jsonObj.get("Bomeninventaris");
            for(Object parking : trees){
                treeDatas.add(new MyTreeData(parking, runtime_latitude, runtime_longitude));
            }
            log.info("sorting...");
            Collections.sort(treeDatas);
            log.info("sorting done");
        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
            return new EmptyTestResult();
        }
        log.info("Best spot is " + treeDatas.get(0));
        JSONArray jsonArray = new JSONArray();
        for(MyTreeData treeData : treeDatas){
            if(showAll || treeData.distance < Utils.getDouble(getProperty(DISTANCE)))
                jsonArray.add(treeData.getAsJSON());
        }
        jsonObject.put("locations", jsonArray);
        jsonObject.put("bestLocation", jsonArray.get(0));


        log.info("total number of trees=" + treeDatas.size());
        log.info("added trees=" + jsonArray.size());
        //log.debug("raw data is "+jsonObject.toJSONString());


        final String state;
        if(treeDatas.get(0).distance  < Utils.getDouble(getProperty(DISTANCE)))
            state = states[0];
        else
            state = states[1];


        final JSONObject finalJsonObject = jsonObject;
        return new TestResult() {
            @Override
            public boolean isSuccess() {
                return true;
            }

            @Override
            public String getName() {
                return "Location result";
            }

            @Override
            public String getObserverState() {
                return state;
            }

            @Override
            public List<Map<String, Number>> getObserverStates() {
                return null;
            }

            @Override
            public String getRawData() {
                return finalJsonObject.toJSONString();
            }
        };

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

    public static void main(String []args) throws ParseException {
        TreeSensor locationSensor = new TreeSensor();
        locationSensor.setProperty(DISTANCE, 3);
        locationSensor.setProperty(CITY, "Gent");
        TestSessionContext testSessionContext = new TestSessionContext(1);
        testSessionContext.setAttribute(RUNTIME_LONGITUDE, 3.68);
        testSessionContext.setAttribute(RUNTIME_LATITUDE, 50.99);
        TestResult testResult = locationSensor.execute(testSessionContext);
        System.out.println(testResult.getObserverState());
        //System.out.println(testResult.getRawData());
    }

    private class MyTreeData implements Comparable{
        String name;
        String nameLatin;
        String code;
        String ID;
        Double latitude;
        Double longitude;
        Long diameter;
        Long height;
        Long year;
        Integer distance;
        Double formulaCalc;
        String address;

        public MyTreeData(Object parking, Double runtime_latitude, Double runtime_longitude) {
            JSONObject obj = (JSONObject) parking;
            name = (String) obj.get("naamnl");
            nameLatin = (String) obj.get("naam");
            ID = (String) obj.get("boomid");
            code = (String) obj.get("boomcode");
            latitude = Utils.getDouble(obj.get("lat"));
            longitude = Utils.getDouble(obj.get("long"));
            year = Utils.getDouble(obj.get("plantjaar")).longValue();
            try{
                diameter = Utils.getDouble(obj.get("stamdiamet").toString().split("-")[0].trim()).longValue();
                if(obj.get("boomhoogte").toString().indexOf("-") > -1)
                    height = Utils.getDouble(obj.get("boomhoogte").toString().split("-")[0].trim()).longValue();
                else
                    height = Utils.getDouble(obj.get("boomhoogte").toString().replace(">","").replace("m","").trim()).longValue();
            } catch (Exception e){
                log.warn(e.getMessage());
                e.printStackTrace();
            }

            distance = FormulaParser.calculateDistance(runtime_latitude, runtime_longitude,
                    latitude, longitude);

            formulaCalc = 1d/distance;
            address = "ID="+ID +", code="+code  +", name="+nameLatin +", height="+height+"," +
                    " diameter="+diameter;
            //TreeSensor.this.log.debug(this.toString());
        }

        @Override
        public int compareTo(Object o) {
            MyTreeData other = (MyTreeData) o;
            return other.formulaCalc.compareTo(formulaCalc);
        }

        @Override
        public String toString() {
            return "MyTreeData{" +
                    "address="+address+  '\'' +
                    ", year=" + year +  '\'' +
                    ", name='" + name + '\'' +
                    ", nameLatin='" + nameLatin + '\'' +
                    ", code='" + code + '\'' +
                    ", ID='" + ID + '\'' +
                    ", latitude=" + latitude +
                    ", longitude=" + longitude +
                    ", diameter=" + diameter +
                    ", height=" + height +
                    '}';
        }

        public JSONObject getAsJSON(){
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("name", name);
            jsonObject.put("nameLatin", nameLatin);
            jsonObject.put("code", code);
            jsonObject.put("latitude", latitude);
            jsonObject.put("longitude", longitude);
            jsonObject.put("year", year);
            jsonObject.put("ID", ID);
            jsonObject.put("distance", distance.intValue());
            jsonObject.put("diameter", diameter);
            jsonObject.put("height", height);
            jsonObject.put("icon", "https://maps.gstatic.com/mapfiles/ms2/micons/tree.png");

            jsonObject.put("address", address);
            return jsonObject;
        }
    }
}
