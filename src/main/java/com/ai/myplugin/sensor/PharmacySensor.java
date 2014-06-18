/**
 * Created by User: veselin
 * On Date: 29/01/14
 */

package com.ai.myplugin.sensor;

import com.ai.api.*;
import com.ai.myplugin.util.FormulaParser;
import com.ai.myplugin.util.Rest;
import com.ai.myplugin.util.Utils;

import com.ai.api.SensorPlugin;
import com.ai.api.SensorResult;
import com.ai.api.SessionContext;
import com.ai.myplugin.util.*;

import net.xeoh.plugins.base.annotations.PluginImplementation;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/*
http://www.coopapotheken.be/wachtdienst_regio.php?regio=Gent
http://datatank.gent.be/Gezondheid/Apotheken.json
<tr>
<td><a href='apotheek_view.php?id=8'>Gentbrugge - Depuydt Guillaume Kerkstraat 196 </a></td>
<td>27-01-14</td>
<td>29-01-14</td>
</tr>
 */
@PluginImplementation
@PluginHeader(version = "1.0.1", author = "Veselin", category = "Smart City", iconURL = "http://app.waylay.io/icons/pharmacy.png")
public class PharmacySensor implements SensorPlugin {

    private static final Logger log = LoggerFactory.getLogger(PharmacySensor.class);

    static final String DISTANCE = "distance";
    static final String CITY = "city";
    static final String RUNTIME_LATITUDE = "runtime_latitude";
    static final String RUNTIME_LONGITUDE = "runtime_longitude";
    static final String LOCATION = "location";
    static final String LATITUDE = "latitude";
    static final String LONGITUDE = "longitude";

    Map<String, Object> propertiesMap = new ConcurrentHashMap<String, Object>();

    String [] states = {"Found", "Not Found"};
    private static final String NAME = "Pharmacy";


    @Override
    public Map<String, PropertyType> getRequiredProperties() {
        Map<String, PropertyType> map = new HashMap<>();
        map.put(CITY, new PropertyType(DataType.STRING, true, false));
        map.put(LOCATION, new PropertyType(DataType.STRING, true, false));
        map.put(LATITUDE, new PropertyType(DataType.DOUBLE, true, false));
        map.put(LONGITUDE, new PropertyType(DataType.DOUBLE, true, false));
        map.put(DISTANCE, new PropertyType(DataType.DOUBLE, true, false));
        return map;
    }

    @Override
    public Map<String, PropertyType> getRuntimeProperties() {
        Map<String, PropertyType> map = new HashMap<>();
        map.put(RUNTIME_LATITUDE, new PropertyType(DataType.STRING, true, false));
        map.put(RUNTIME_LONGITUDE, new PropertyType(DataType.DOUBLE, true, false));
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
        return "Check for pharmacies that are open during night";
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
            setProperty(LOCATION, getProperty(CITY));   */

        LatLng latLng;
        try {
            latLng = Utils.getLocation(testSessionContext, getProperty(LOCATION), getProperty(LONGITUDE), getProperty(LATITUDE));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return SensorResultBuilder.failure().build();
        }

        log.info("Current location: " + latLng);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(RUNTIME_LATITUDE, latLng.latitude);
        jsonObject.put(RUNTIME_LONGITUDE, latLng.longitude);

        String pathURL = "http://datatank.gent.be/Gezondheid/Apotheken.json";
        ArrayList<MyPharmacyData> myPharmacyDatas = new ArrayList<MyPharmacyData>();
        try{
            JSONObject apothekenObject = Rest.httpGet(pathURL).json();
            JSONArray pharmaArray = (JSONArray)(apothekenObject.get("Apotheken"));
            for(Object pharma : pharmaArray){
                myPharmacyDatas.add(new MyPharmacyData(pharma, latLng.latitude, latLng.longitude));
            }
            Collections.sort(myPharmacyDatas);
        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
            return SensorResultBuilder.failure().build();
        }

        pathURL = "http://www.coopapotheken.be/wachtdienst_regio.php?regio="+getProperty(CITY);
        String stringToParse;
        ArrayList<MyNightPharmacyData> myNightPharmacyDatas = new ArrayList<MyNightPharmacyData>();
        try{
            stringToParse = Rest.httpGet(pathURL).body();
            log.info(stringToParse);

            Document doc = Jsoup.parse(stringToParse);
            for (Element table : doc.select("table[class=tekst]")) {
                for (Element row : table.select("tr")) {
                    Elements tds = row.select("td");
                    if (tds.size() >2 ) {
                        StringTokenizer stringTokenizer = new StringTokenizer(tds.get(0).text(), "-");
                        if(stringTokenizer.countTokens() == 2)      {
                            String city = stringTokenizer.nextToken().trim();
                            String street = stringTokenizer.nextToken().trim();
                            String begin = tds.get(1).text().trim();
                            String end = tds.get(2).text().trim();
                            String url = "http://www.coopapotheken.be/" + row.select("a[href]").attr("href");
                            Date currentDate = new Date();
                            SimpleDateFormat dt1 = new SimpleDateFormat("dd-MM-yy");
                            Date beginDate = dt1.parse(begin);
                            Date endDate = dt1.parse(end);
                            if(currentDate.compareTo(beginDate) >= 0 && currentDate.compareTo(endDate) <= 0){
                                log.info("found entry that is within a current day " +street);
                                myNightPharmacyDatas.add(new MyNightPharmacyData(city, street, url, beginDate, endDate, myPharmacyDatas));
                            } else{
                                log.debug("entry not to be added since it is not in the current data window " + street
                                        + ", current date is " +currentDate + ", end found was: "  +beginDate + ", " + endDate);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return SensorResultBuilder.failure().build();
        }
        log.info("Best spot is "+myPharmacyDatas.get(0));
        JSONArray jsonArray = new JSONArray();
        for(MyPharmacyData data : myPharmacyDatas){
            jsonArray.add(data.getAsJSON());

        }
        JSONArray jsonNightArray = new JSONArray();
        for(MyNightPharmacyData data : myNightPharmacyDatas){
            jsonNightArray.add(data.getAsJSON());

        }
        jsonObject.put("locations", jsonArray);
        jsonObject.put("nightLocations", jsonNightArray);
        jsonObject.put("bestLocation", jsonArray.get(0));
        if(myNightPharmacyDatas.size() > 0)
            jsonObject.put("bestNightLocation", jsonNightArray.get(0));
        log.info("raw data is "+jsonObject.toJSONString());


        final String state;
        //if(myNightPharmacyDatas.get(0).distance  < Utils.getDouble(getProperty(DISTANCE)))
        if(myPharmacyDatas.get(0).distance  < Utils.getDouble(getProperty(DISTANCE)))
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

    private class MyNightPharmacyData implements Comparable{
        String address;
        String city;
        String real_address;
        Double latitude;
        Double longitude;
        Integer distance;
        String mapURL;
        String URL;

        public MyNightPharmacyData(String city, String address, String url, Date startDate, Date endDate, ArrayList<MyPharmacyData> datas) {
            PharmacySensor.this.log.info("MyNightPharmacyData" + city + ", " + address + ", " + startDate + ", " + endDate);
            this.address = address;
            this.city = city;
            this.URL = url;
            int addNumber = getNumberFromAddress(address);
            distance = Integer.MAX_VALUE;

            for(MyPharmacyData data: datas){
                String []splitAddress = data.address.split(" ");
                ArrayList<String> strings = new ArrayList<String>();
                int number = -2;
                for(String str : splitAddress){
                    try{
                        number = Integer.parseInt(str);
                    } catch (Exception e){
                        strings.add(str);
                    }
                }
                if(addNumber == number){
                    for(String split : strings){
                        if(address.toLowerCase().indexOf(split.toLowerCase()) > 0 ||
                                address.toLowerCase().indexOf(split.replaceAll("-"," ").toLowerCase()) > 0 ||
                                address.replaceAll("St\\. ", "Sint-").toLowerCase().indexOf(split.toLowerCase()) > 0 ||
                                address.contains("Fonteineplein") && data.address.contains("Fonteyneplein"))
                                /*&&  city.equalsIgnoreCase(data.city)*/ {
                            latitude = data.latitude;
                            longitude = data.longitude;
                            mapURL = data.mapURL;
                            distance = data.distance;
                            real_address =  data.address;
                            PharmacySensor.this.log.info("Found entry to add in the night pharmacies" + this.toString());
                            break;
                        }
                    }
                }
            }
        }

        @Override
        public int compareTo(Object o) {
            MyNightPharmacyData other = (MyNightPharmacyData) o;
            return other.distance.compareTo(distance);
        }

        @Override
        public String toString() {
            return "MyNightPharmacyData{" +
                    "address='" + address + '\'' +
                    "real_address='" + real_address + '\'' +
                    ", latitude=" + latitude +
                    ", longitude=" + longitude +
                    ", distance=" + distance +
                    ", mapURL='" + mapURL + '\'' +
                    '}';
        }

        public JSONObject getAsJSON(){
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("address", address.replace("<br>"," "));
            jsonObject.put("url", mapURL);
            jsonObject.put("web_url", URL);
            jsonObject.put("latitude", latitude);
            jsonObject.put("longitude", latitude);
            jsonObject.put("distance", distance.intValue());
            return jsonObject;
        }
    }

    private int getNumberFromAddress(String address) {
        int number = -1;
        String [] splitAddress = address.split(" ");
        for(String str : splitAddress){
            try{
                number = Integer.parseInt(str);
            } catch (Exception e){
            }
        }
        return number;
    }

    private class MyPharmacyData implements Comparable{
        String address;
        Double latitude;
        Double longitude;
        String name;
        String city;
        Integer distance;
        Double formulaCalc;
        String mapURL;
        public MyPharmacyData(Object pharmacy, Double runtime_latitude, Double runtime_longitude) {
            JSONObject obj = (JSONObject) pharmacy;
            address = (String) obj.get("adres");
            latitude = Utils.getDouble(obj.get("lat"));
            longitude = Utils.getDouble(obj.get("long"));
            name = (String)obj.get("naam");
            city = (String)(obj.get("gemeente"));
            mapURL = "https://maps.google.com/maps?q="  +latitude + "," + longitude ;

            distance = FormulaParser.calculateDistance(runtime_latitude, runtime_longitude,
                    latitude, longitude);

            formulaCalc = 1d/distance;
            PharmacySensor.this.log.info(this.toString());
        }

        @Override
        public int compareTo(Object o) {
            MyPharmacyData other = (MyPharmacyData) o;
            return other.formulaCalc.compareTo(formulaCalc);
        }

        @Override
        public String toString() {
            return "MyPharmacyData{" +
                    "address='" + address + '\'' +
                    ", latitude=" + latitude +
                    ", longitude=" + longitude +
                    ", name=" + name +
                    ", city=" + city +
                    ", distance=" + distance +
                    ", formulaCalc=" + formulaCalc +
                    ", mapURL='" + mapURL + '\'' +
                    '}';
        }

        public JSONObject getAsJSON(){
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("address", address);
            jsonObject.put("url", mapURL);
            jsonObject.put("web_url", mapURL);
            jsonObject.put("latitude", latitude);
            jsonObject.put("longitude", longitude);
            jsonObject.put("distance", distance.intValue());
            return jsonObject;
        }
    }

    public static void main(String []args ) {
        PharmacySensor pharmacySensor = new PharmacySensor();
        pharmacySensor.setProperty(CITY, "Gent");
        pharmacySensor.setProperty(DISTANCE, 10);
        SessionContext testSessionContext = new SessionContext(1);
        testSessionContext.setAttribute(RUNTIME_LONGITUDE, 3.68);
        testSessionContext.setAttribute(RUNTIME_LATITUDE, 50.99);
        SensorResult testResult = pharmacySensor.execute(testSessionContext);
        System.out.println(testResult.getRawData());
    }


}
