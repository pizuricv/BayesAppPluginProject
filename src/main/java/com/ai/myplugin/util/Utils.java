package com.ai.myplugin.util;

import com.ai.api.SessionContext;
import com.ai.myplugin.sensor.LocationRawSensor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Utils {
    private static final Log log = LogFactory.getLog(Utils.class);
    private static final String RUNTIME_LATITUDE = "runtime_latitude";
    private static final String RUNTIME_LONGITUDE = "runtime_longitude";

    public static Double getDouble(Object obj){
        if(obj == null){
            return null;
        }

        Number number;
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

    public static Geocoder.LatLng getLocation(SessionContext testSessionContext, Object location,
                                                  Object longitude, Object latitude) throws Exception {

        Optional<Geocoder.LatLng> runtimeLatLng = getRuntimeLatLng(testSessionContext);

        // ugly syntax because there is no or in Java8 :-s

        return runtimeLatLng
                .orElseGet(() -> getProvidedLatLng(latitude, longitude)
                        .orElseGet(() -> getLatLngByLocation(location)
                                .orElseThrow(() -> new RuntimeException("latitude, longitude and/or location not configured"))));
    }

    private static Optional<Geocoder.LatLng> getProvidedLatLng(Object latitude, Object longitude){
        if (latitude == null || longitude == null) {
            return Optional.empty();
        }else{
            return Optional.of(new Geocoder.LatLng(Utils.getDouble(latitude), Utils.getDouble(longitude)));
        }
    }

    private static Optional<Geocoder.LatLng> getLatLngByLocation(Object location){
        if (location != null) {
            log.info("Location configured as the address: " + location + " , try to get coordinates");
            Geocoder.LatLng latLng = Geocoder.getLongitudeLatitudeForAddress(location.toString());
            log.info("Use location: " + latLng);
            return Optional.of(latLng);
        } else {
            return Optional.empty();
        }
    }

    private static Optional<Geocoder.LatLng> getRuntimeLatLng(SessionContext context){
        Double runtimeLatitude = null;
        Double runtimeLongitude = null;
        if(context != null){
            runtimeLatitude = Utils.getDouble(context.getAttribute(RUNTIME_LATITUDE));
            runtimeLongitude = Utils.getDouble(context.getAttribute(RUNTIME_LONGITUDE));
        }
        if(runtimeLatitude == null || runtimeLongitude == null){
            return Optional.empty();
        }else{
            return Optional.of(new Geocoder.LatLng(runtimeLatitude, runtimeLongitude));
        }
    }
}
