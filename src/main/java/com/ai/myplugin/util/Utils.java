package com.ai.myplugin.util;

import com.ai.myplugin.sensor.LocationRawSensor;
import com.ai.util.resource.TestSessionContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Utils {
    private static final Log log = LogFactory.getLog(Utils.class);
    private static final String RUNTIME_LATITUDE = "runtime_latitude";
    private static final String RUNTIME_LONGITUDE = "runtime_longitude";

    public static Double getDouble(Object obj){
        Number number = null;
        if(obj instanceof String){
            try {
                number = Double.parseDouble((String) obj);
            } catch(NumberFormatException e) {
                try {
                    number = Float.parseFloat((String) obj);
                } catch(NumberFormatException e1) {
                    try {
                        number = Long.parseLong((String) obj);
                    } catch(NumberFormatException e2) {
                        try {
                            number = Integer.parseInt((String) obj);
                        } catch(NumberFormatException e3) {
                            throw e3;
                        }
                    }
                }
            }
        } else{
            number = (Number) obj;
        }
        return number.doubleValue();
    }

    public static void main(String []args) {
        log.debug(Utils.getDouble(new Integer(23)));
        log.debug(Utils.getDouble(new Double(23)));
        log.debug(Utils.getDouble("23"));
        log.debug(Utils.getDouble(23));
    }

    public static Map<Double, Double> getLocation(TestSessionContext testSessionContext, Object location,
                                                  Object longitude, Object latitude) throws Exception {

        Map<Double, Double> mapLocation = new HashMap<Double, Double>();
        Object rt1 = null;
        Object rt2 = null;
        if(testSessionContext != null){
            rt1 = testSessionContext.getAttribute(RUNTIME_LATITUDE);
            rt2 = testSessionContext.getAttribute(RUNTIME_LONGITUDE);
        }
        if(rt1 == null || rt2 == null){
            log.warn("no runtime longitude or latitude given, it will use configured location instead");
            if(latitude == null || longitude == null){
                if(location != null){
                    log.info("Location configured as the address: " + location +  " , try to get coordinates");
                    JSONObject jsonObject;
                    try {
                        jsonObject = LocationRawSensor.getLongitudeLatitudeForAddress(location.toString());
                    } catch (Exception e) {
                        String message = "location could not be found "+ e.getMessage();
                        e.printStackTrace();
                        log.error(message);
                        throw new Exception(message);
                    }
                    double lon = Utils.getDouble(jsonObject.get("longitude"));
                    double lat = Utils.getDouble(jsonObject.get("latitude"));
                    log.info("Use configured location: "+ lat + ","+lon);
                    mapLocation.put(lat, lon);
                } else {
                    String message = "longitude or latitude not configured";
                    log.error(message);
                    throw new Exception(message);
                }
            } else{
                double lat = Utils.getDouble(latitude);
                double lon = Utils.getDouble(longitude);
                log.info("Use configured location: "+ lat + ","+lon);
                mapLocation.put(lat, lon);
            }
        } else {
            double lat = Utils.getDouble(rt1);
            double lon = Utils.getDouble(rt2);
            log.info("Use runtime location: "+ lat + ","+lon);
            mapLocation.put(lat, lon);
        }
        return mapLocation;
    }
}
