package com.ai.myplugin.util;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mashape geocoder
 */
public final class Geocoder {

    private Geocoder(){
        throw new UnsupportedOperationException("Utility class");
    }

    public static LatLng getLongitudeLatitudeForAddress(String address){
        String url = "https://montanaflynn-geocode-location-information.p.mashape.com/address?address=" + URLEncoder.encode(address);
        //String url = "https://metropolis-api-geocode.p.mashape.com/solve?address=" + URLEncoder.encode(address);

        Map<String, String> httpSettings = new ConcurrentHashMap<String, String>();
        httpSettings.put("X-Mashape-Authorization", APIKeys.getMashapeKey());

        JSONObject response = null;
        try {
            response = Rest.httpGet(url, httpSettings).json();
        } catch (ParseException| IOException e) {
            throw new RuntimeException(e);
        }

        //curl "https://maps.googleapis.com/maps/api/geocode/json?address=1600+Amphitheatre+Parkway,+Mountain+View,+CA&sensor=false&key=AIzaSyAB4NA8aZi1wXgKRbMN8Z5BdNm7NkI9nb0"

        /*String url =  "https://maps.googleapis.com/maps/api/geocode/json?address="  + URLEncoder.encode(address) +
                "&sensor=false&key="+APIKeys.getGoogleKey(); */
        //String ret = Rest.httpGet(url);

        // TODO Why this getDouble, to support String and other json numbers, ugly?
        double latitude = Utils.getDouble(response.get("latitude"));
        double longitude = Utils.getDouble(response.get("longitude"));

        return new LatLng(latitude, longitude);
    }

    public static final class LatLng{
        public final double latitude;
        public final double longitude;

        public LatLng(final double latitude, final double longitude){
            this.latitude = latitude;
            this.longitude = longitude;
        }

        @Override
        public String toString() {
            return "LatLng{" +
                    "latitude=" + latitude +
                    ", longitude=" + longitude +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            LatLng latLng = (LatLng) o;

            if (Double.compare(latLng.latitude, latitude) != 0) return false;
            if (Double.compare(latLng.longitude, longitude) != 0) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result;
            long temp;
            temp = Double.doubleToLongBits(latitude);
            result = (int) (temp ^ (temp >>> 32));
            temp = Double.doubleToLongBits(longitude);
            result = 31 * result + (int) (temp ^ (temp >>> 32));
            return result;
        }
    }
}
