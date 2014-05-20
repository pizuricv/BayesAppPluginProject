/**
 * Created by User: veselin
 * On Date: 26/12/13
 */

package com.ai.myplugin.sensor;

import com.ai.api.SensorPlugin;
import com.ai.api.SensorResult;
import com.ai.api.SessionContext;
import com.ai.myplugin.util.*;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@PluginImplementation
public class ParkingSensor implements SensorPlugin {
    protected static final Log log = LogFactory.getLog(ParkingSensor.class);
    static final String DISTANCE = "distance";
    static final String CITY = "city";
    static final String RUNTIME_LATITUDE = "runtime_latitude";
    static final String RUNTIME_LONGITUDE = "runtime_longitude";
    static final String LOCATION = "location";
    static final String LATITUDE = "latitude";
    static final String LONGITUDE = "longitude";

    Map<String, Object> propertiesMap = new ConcurrentHashMap<String, Object>();

    String [] states = {"Found", "Not Found"};
    private static final String NAME = "ParkingSensor";

    @Override
    public String[] getRequiredProperties() {
        return new String[]{DISTANCE, CITY, LOCATION, LATITUDE, LONGITUDE};
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
    public SensorResult execute(SessionContext testSessionContext) {
        log.info("execute "+ getName() + ", sensor type:" +this.getClass().getName());
        if(getProperty(DISTANCE) == null)
            throw new RuntimeException("distance not set");
        if(getProperty(CITY) == null)
            throw new RuntimeException("city not set");
        if(!getProperty(CITY).toString().equalsIgnoreCase("Gent"))
            throw new RuntimeException("only Gent supported for now");

        /*if(getProperty(LOCATION) == null)
            setProperty(LOCATION, getProperty(CITY));       */

        Map<Double, Double> map;
        try {
            map = Utils.getLocation(testSessionContext, getProperty(LOCATION),
                    getProperty(LONGITUDE), getProperty(LATITUDE));
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            return new EmptySensorResult();
        }
        Double latitude = (Double) map.keySet().toArray()[0];
        Double longitude = (Double) map.values().toArray()[0];

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(RUNTIME_LATITUDE, latitude);
        jsonObject.put(RUNTIME_LONGITUDE, longitude);

        String pathURL = "http://datatank.gent.be/Mobiliteitsbedrijf/Parkings11.json";
        ArrayList<MyParkingData> parkingDatas = new ArrayList<MyParkingData>();
        try{
            JSONObject parkingObj = Rest.httpGet(pathURL).json();
            JSONArray parkings = ((JSONArray)((JSONObject) (parkingObj.get("Parkings11"))).get("parkings"));
            for(Object parking : parkings){
                parkingDatas.add(new MyParkingData(parking, latitude, longitude));
            }
            Collections.sort(parkingDatas);
        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
            return new EmptySensorResult();
        }
        log.info("Best spot is "+parkingDatas.get(0));
        JSONArray jsonArray = new JSONArray();
        for(MyParkingData parkingData : parkingDatas){
            jsonArray.add(parkingData.getAsJSON());

        }
        jsonObject.put("locations", jsonArray);
        jsonObject.put("bestLocation", jsonArray.get(0));


        //log.info("Computed parking: " + Arrays.asList(parkingDatas).toString());
        log.info("raw data is "+jsonObject.toJSONString());


        final String state;
        if(parkingDatas.get(0).distance  < Utils.getDouble(getProperty(DISTANCE)))
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
    public String[] getSupportedStates() {
        return states;
    }

    public static void main(String []args) throws ParseException {
        ParkingSensor locationSensor = new ParkingSensor();
        locationSensor.setProperty(DISTANCE, 10);
        locationSensor.setProperty(CITY, "Gent");
        SessionContext testSessionContext = new SessionContext(1);
        testSessionContext.setAttribute(RUNTIME_LONGITUDE, 3.68);
        testSessionContext.setAttribute(RUNTIME_LATITUDE, 50.99);
        SensorResult testResult = locationSensor.execute(testSessionContext);
        System.out.println(testResult.getObserverState());
        System.out.println(testResult.getRawData());

        locationSensor.setProperty(DISTANCE, 10);
        locationSensor.setProperty(CITY, "Gent");
        locationSensor.setProperty(LOCATION, "Gent");
        testResult = locationSensor.execute(testSessionContext);
        System.out.println(testResult.getObserverState());
        System.out.println(testResult.getRawData());
    }

    private class MyParkingData implements Comparable{
        String address;
        Double latitude;
        Double longitude;
        Double capacity;
        Double free;
        Integer distance;
        Double formulaCalc;
        String mapURL;
        public MyParkingData(Object parking, Double runtime_latitude, Double runtime_longitude) {
            JSONObject obj = (JSONObject) parking;
            address = (String) obj.get("address");
            latitude = Utils.getDouble(obj.get("latitude"));
            longitude = Utils.getDouble(obj.get("longitude"));
            capacity = Utils.getDouble(obj.get("totalCapacity"));
            //jesus
            try{
                free = Utils.getDouble(obj.get("availableCapacity"));
            } catch (Exception e){
                //"availableCapacity":"VOL", that is full in Dutch :)
                free = 0.0;
            }
            mapURL = "https://maps.google.com/maps?q="  +latitude + "," + longitude ;


            distance = FormulaParser.calculateDistance(runtime_latitude, runtime_longitude,
                    latitude, longitude);

            if(free < 10)
                formulaCalc = 1d/distance * free/capacity;
            else
                formulaCalc = 1d/distance;
            ParkingSensor.this.log.info(this.toString());
        }

        @Override
        public int compareTo(Object o) {
            MyParkingData other = (MyParkingData) o;
            return other.formulaCalc.compareTo(formulaCalc);
        }

        @Override
        public String toString() {
            return "MyParkingData{" +
                    "address='" + address + '\'' +
                    ", latitude=" + latitude +
                    ", longitude=" + longitude +
                    ", capacity=" + capacity +
                    ", free=" + free +
                    ", distance=" + distance +
                    ", formulaCalc=" + formulaCalc +
                    ", mapURL='" + mapURL + '\'' +
                    '}';
        }

        public JSONObject getAsJSON(){
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("address", address.replace("<br>"," "));
            jsonObject.put("url", mapURL);
            jsonObject.put("latitude", latitude);
            jsonObject.put("longitude", longitude);
            jsonObject.put("capacity", capacity.intValue());
            jsonObject.put("free", free.intValue());
            jsonObject.put("distance", distance.intValue());
            return jsonObject;
        }
    }
}
