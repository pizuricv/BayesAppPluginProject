/**
 * Created by User: veselin
 * On Date: 26/12/13
 */
package com.ai.myplugin.sensor;

import com.ai.api.*;
import com.ai.myplugin.util.*;
import net.xeoh.plugins.base.annotations.PluginImplementation;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@PluginImplementation
@PluginHeader(version = "1.0.1", author = "Veselin", category = "Smart City", iconURL = "http://app.waylay.io/icons/tree.png")
public class TreeSensor implements SensorPlugin {

    private static final Logger log = LoggerFactory.getLogger(TreeSensor.class);

    static final String DISTANCE = "distance";
    static final String CITY = "city";
    static final String SHOW_ALL = "showAll";
    static final String RUNTIME_LATITUDE = "latitude";
    static final String RUNTIME_LONGITUDE = "longitude";
    static final String LOCATION = "location";
    static final String LATITUDE = "latitude";
    static final String LONGITUDE = "longitude";

    private static final String pathURL = "http://datatank.gent.be/MilieuEnNatuur/Bomeninventaris.json";

    boolean showAll = false;

    Map<String, Object> propertiesMap = new ConcurrentHashMap<String, Object>();

    String [] states = {"Found", "Not Found"};
    private static final String NAME = "TreeSensor";


    @Override
    public Map<String, PropertyType> getRequiredProperties() {
        Map<String, PropertyType> map = new HashMap<>();
        map.put(CITY, new PropertyType(DataType.STRING, true, false));
        map.put(SHOW_ALL, new PropertyType(DataType.BOOLEAN, true, false));
        map.put(LOCATION, new PropertyType(DataType.STRING, true, false));
        map.put(LATITUDE, new PropertyType(DataType.DOUBLE, true, false));
        map.put(LONGITUDE, new PropertyType(DataType.DOUBLE, true, false));
        map.put(DISTANCE, new PropertyType(DataType.DOUBLE, true, false));
        return map;
    }

    @Override
    public Map<String, RawDataType> getRequiredRawData() {
        Map<String, RawDataType> map = new HashMap<>();
        map.put(RUNTIME_LATITUDE, new RawDataType("deg", DataType.DOUBLE));
        map.put(RUNTIME_LONGITUDE, new RawDataType("deg", DataType.DOUBLE));
        return map;
    }

    @Override
    public void setProperty(String string, Object obj) {
        if(getRequiredProperties().keySet().contains(string)) {
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
        return "Gives location of all trees in Gent";
    }

    @Override
    public SensorResult execute(SessionContext testSessionContext) {
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

        LatLng latLng;
        try {
            latLng = Utils.getLocation(testSessionContext, getProperty(LOCATION),
                    getProperty(LONGITUDE), getProperty(LATITUDE));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return SensorResultBuilder.failure(e.getMessage()).build();
        }

        log.info("Current location: " + latLng);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(RUNTIME_LATITUDE, latLng.latitude);
        jsonObject.put(RUNTIME_LONGITUDE, latLng.longitude);

        ArrayList<MyTreeData> treeDatas = new ArrayList<MyTreeData>();
        try{
            JSONObject jsonObj = Rest.httpGet(pathURL).json();
            JSONArray trees = (JSONArray)jsonObj.get("Bomeninventaris");
            for(Object parking : trees){
                treeDatas.add(new MyTreeData(parking, latLng.latitude, latLng.longitude));
            }
            log.info("sorting...");
            Collections.sort(treeDatas);
            log.info("sorting done");
        } catch (Exception e) {
            log.error(e.getLocalizedMessage(), e);
            return SensorResultBuilder.failure(e.getMessage()).build();
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
        return new SensorResult() {
            @Override
            public boolean isSuccess() {
                return true;
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
    public void setup(SessionContext testSessionContext) {
        log.debug("Setup : " + getName() + ", sensor : "+this.getClass().getName());
    }

    @Override
    public void shutdown(SessionContext testSessionContext) {

    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Set<String> getSupportedStates() {
        return new HashSet(Arrays.asList(states));
    }

    public static void main(String []args) throws ParseException {
        TreeSensor locationSensor = new TreeSensor();
        locationSensor.setProperty(DISTANCE, 3);
        locationSensor.setProperty(CITY, "Gent");
        SessionContext testSessionContext = new SessionContext(1);
        testSessionContext.setAttribute(RUNTIME_LONGITUDE, 3.68);
        testSessionContext.setAttribute(RUNTIME_LATITUDE, 50.99);
        SensorResult testResult = locationSensor.execute(testSessionContext);
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
                log.warn(e.getMessage(), e);
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
            //alergies

            if(name.contains("schietwilg") || name.contains("treurwilg") ||
                    name.contains("artbladige els") || name.contains("rode els") ||
                    name.contains("boomhazelaar"))
                jsonObject.put("icon", "https://maps.gstatic.com/mapfiles/ms2/micons/yellow-dot.png");
            else
                jsonObject.put("icon", "https://maps.gstatic.com/mapfiles/ms2/micons/tree.png");

            jsonObject.put("address", address);
            return jsonObject;
        }
    }
}
