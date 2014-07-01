/**
 * Created with IntelliJ IDEA.
 * User: veselin
 * Date: 10/09/13
 */

package com.ai.myplugin.sensor;
import com.ai.api.*;
import com.ai.myplugin.util.Rest;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;


@PluginImplementation
@PluginHeader (version = "1.0.1", author = "Veselin", category = "Environment", iconURL = "http://app.waylay.io/icons/uvindex.png")
public class UVSensor implements SensorPlugin {

    private static final Logger log = LoggerFactory.getLogger(UVSensor.class);

    private static String ZIPCODE = "zipcode";
    private static String zipCode = null;
    static final String server = "http://iaspub.epa.gov/enviro/efservice/getEnvirofactsUVHOURLY/ZIP/";
    private String[] states = {"Green", "Yellow", "Orange", "Red", "Violet"};

    @Override
    public Map<String,PropertyType> getRequiredProperties() {
        Map<String, PropertyType> map = new HashMap<>();
        map.put(ZIPCODE, new PropertyType(DataType.STRING, true, false));
        return map;
    }

    @Override
    public void setProperty(String s, Object o) {
        if(ZIPCODE.equals(s)) {
            zipCode = (String) o;
        } else
            throw new RuntimeException("Property "+ s + " not in the required settings");
    }

    @Override
    public Object getProperty(String s) {
        if(ZIPCODE.equals(s)) {
            return zipCode;
        } else
            return null;
    }

    @Override
    public String getDescription() {
        return "UV index sensor, right now only for US, provide zip code as the entry e.g. 20050 for Washington DC";
    }

    @Override
    public SensorResult execute(SessionContext testSessionContext) {
        log.debug("execute " + getName() + ", sensor type:" + this.getClass().getName());

        if(zipCode == null){
            throw new RuntimeException("Zip code not defined");
        }

        String pathURL = server + zipCode + "/JSON";

        boolean testSuccess = true;
        JSONArray array = new JSONArray();

        try{
            array = Rest.httpGet(pathURL).jsonArray();
        } catch (ParseException|IOException e) {
            testSuccess = false;
        }

        List<UVObject>  list = new ArrayList<UVObject> ();

        if(testSuccess ){
            for(Object obj : array){
                JSONObject o = (JSONObject) obj;
                try {
                    list.add(new UVObject((String) o.get("DATE_TIME"), (Long)o.get("ORDER"),
                            (Long) o.get("UV_VALUE")));
                } catch (java.text.ParseException e) {
                    e.printStackTrace();
                    return new SensorResult() {
                        @Override
                        public boolean isSuccess() {
                            return false;  //To change body of implemented methods use File | Settings | File Templates.
                        }

                        @Override
                        public String getObserverState() {
                            return null;  //To change body of implemented methods use File | Settings | File Templates.
                        }

                        @Override
                        public List<Map<String, Number>> getObserverStates() {
                            return null;  //To change body of implemented methods use File | Settings | File Templates.
                        }

                        @Override
                        public String getRawData() {
                            return null;  //To change body of implemented methods use File | Settings | File Templates.
                        }
                    };
                }
            }
            /*
            {
        "ORDER": 1,
        "ZIP": 20050,
        "DATE_TIME": "SEP/10/2013 07 AM",
        "UV_VALUE": 0
    },
             */

        }
        Collections.sort(list);
        //log.debug(list.toString());
        // TODO fix this later, once you know more about the order and you also need to get the right time zone from the zip code
        //let's take the max, otherwise really no idea, the ordering is a mess
        Long max = -1l;
        for(UVObject uvObject : list) {
            if(uvObject.uvIndex > max)
                max = uvObject.uvIndex;
        }

        return mapToState(max);
    }

    //http://en.wikipedia.org/wiki/Ultraviolet_index

    private SensorResult mapToState(final Long max) {
        return new SensorResult() {
            @Override
            public boolean isSuccess() {
                return true;
            }

            @Override
            public String getObserverState() {
                if(max < 3)
                    return "Green";
                if(max < 6)
                    return "Yellow";
                if(max < 8)
                    return "Orange";
                if(max < 11)
                    return "Red";
                return "Violet";
            }

            @Override
            public List<Map<String, Number>> getObserverStates() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public String getRawData() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }
        };
    }

    @Override
    public String getName() {
        return "UVindex";
    }

    @Override
    public Set<String> getSupportedStates() {
        return new HashSet(Arrays.asList(states));
    }

    private class UVObject implements Comparable{
        GregorianCalendar date;
        Long order;
        Long uvIndex;
        SimpleDateFormat format = new SimpleDateFormat("MMM/dd/yyyy hh aa");

        private UVObject(String dateF, Long order, Long uvIndex) throws java.text.ParseException {
            date =  new GregorianCalendar();
            date.setTime(format.parse(dateF));
            this.order = order;
            this.uvIndex = uvIndex;
        }

        @Override
        public String toString() {
            return "UVObject{" +
                    "date=" + date.getTime() +
                    ", order=" + order +
                    ", uvIndex=" + uvIndex +
                    '}';
        }

        @Override
        public int compareTo(Object o) {
            //return order.compareTo(((UVObject)o).order);
            // it is not the same as time??????
            return date.compareTo(((UVObject)o).date);
        }
    }

    @Override
    public void setup(SessionContext testSessionContext) {
        log.debug("Setup : " + getName() + ", sensor : "+this.getClass().getName());
    }

    @Override
    public void shutdown(SessionContext testSessionContext) {
        log.debug("Shutdown : " + getName() + ", sensor : " + this.getClass().getName());
    }


    public static void main(String []args ) {
        UVSensor uvSensor = new UVSensor();
        uvSensor.setProperty(ZIPCODE, "20050");
        log.debug(uvSensor.execute(null).getObserverState());
    }
    //http://iaspub.epa.gov/enviro/efservice/getEnvirofactsUVHOURLY/ZIP/20050/JSON
    /*
    [{"ORDER":1,"ZIP":20050,"DATE_TIME":"SEP/10/2013 07 AM","UV_VALUE":0},{"ORDER":2,"ZIP":20050,"DATE_TIME":"SEP/10/2013 08 AM","UV_VALUE":0},{"ORDER":3,"ZIP":20050,"DATE_TIME":"SEP/10/2013 09 AM","UV_VALUE":1},{"ORDER":4,"ZIP":20050,"DATE_TIME":"SEP/10/2013 10 AM","UV_VALUE":3},{"ORDER":5,"ZIP":20050,"DATE_TIME":"SEP/10/2013 11 AM","UV_VALUE":5},{"ORDER":6,"ZIP":20050,"DATE_TIME":"SEP/10/2013 12 PM","UV_VALUE":6},{"ORDER":7,"ZIP":20050,"DATE_TIME":"SEP/10/2013 01 PM","UV_VALUE":7},{"ORDER":8,"ZIP":20050,"DATE_TIME":"SEP/10/2013 02 PM","UV_VALUE":6},{"ORDER":9,"ZIP":20050,"DATE_TIME":"SEP/10/2013 03 PM","UV_VALUE":5},{"ORDER":10,"ZIP":20050,"DATE_TIME":"SEP/10/2013 04 PM","UV_VALUE":3},{"ORDER":11,"ZIP":20050,"DATE_TIME":"SEP/10/2013 05 PM","UV_VALUE":2},{"ORDER":12,"ZIP":20050,"DATE_TIME":"SEP/10/2013 06 PM","UV_VALUE":1},{"ORDER":13,"ZIP":20050,"DATE_TIME":"SEP/10/2013 07 PM","UV_VALUE":0},{"ORDER":14,"ZIP":20050,"DATE_TIME":"SEP/10/2013 08 PM","UV_VALUE":0},{"ORDER":15,"ZIP":20050,"DATE_TIME":"SEP/10/2013 09 PM","UV_VALUE":0},{"ORDER":16,"ZIP":20050,"DATE_TIME":"SEP/10/2013 10 PM","UV_VALUE":0},{"ORDER":17,"ZIP":20050,"DATE_TIME":"SEP/10/2013 11 PM","UV_VALUE":0},{"ORDER":18,"ZIP":20050,"DATE_TIME":"SEP/10/2013 12 AM","UV_VALUE":0},{"ORDER":19,"ZIP":20050,"DATE_TIME":"SEP/10/2013 01 AM","UV_VALUE":0},{"ORDER":20,"ZIP":20050,"DATE_TIME":"SEP/10/2013 02 AM","UV_VALUE":0},{"ORDER":21,"ZIP":20050,"DATE_TIME":"SEP/10/2013 03 AM","UV_VALUE":0}]
     */
}
