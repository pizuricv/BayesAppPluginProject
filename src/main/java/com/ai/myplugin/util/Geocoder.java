package com.ai.myplugin.util;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mashape geocoder
 */
public final class Geocoder {

    private static final Logger log = LoggerFactory.getLogger(Geocoder.class);

    private Geocoder(){
        throw new UnsupportedOperationException("Utility class");
    }

    public static LatLng getLongitudeLatitudeForAddress(String address){
        String url = "https://montanaflynn-geocode-location-information.p.mashape.com/address?address=" + URLEncoder.encode(address);
        //String url = "https://metropolis-api-geocode.p.mashape.com/solve?address=" + URLEncoder.encode(address);

        Map<String, String> httpSettings = new ConcurrentHashMap<String, String>();
        httpSettings.put("X-Mashape-Authorization", APIKeys.getMashapeKey());

        JSONObject response;
        try {
            response = Rest.httpGet(url, httpSettings).json();
        } catch (ParseException| IOException e) {
            log.error(e.getMessage(), e);
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

    public static JSONObject reverseLookupAddress(double longitude, double latitude){
        Map<String, String> map = new ConcurrentHashMap<String, String>();
        map.put("X-Mashape-Authorization", APIKeys.getMashapeKey());
        String latitudeCoordinateStr = "latitude="+ URLEncoder.encode(Double.toString(latitude));
        String longitudeCoordinateStr = "longitude="+ URLEncoder.encode(Double.toString(longitude));
        String url = "https://montanaflynn-geocode-location-information.p.mashape.com/reverse?" + longitudeCoordinateStr + "&"+ latitudeCoordinateStr;

        JSONObject response;
        try {
            response =  Rest.httpGet(url, map).json();
        } catch (ParseException| IOException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }

        // TODO do not return json but parse the result so we don't depend on this specific rest api

        // {"zip":"18661","country":"Saudi Arabia","city":null,"latitude":19.786089,"street_number":null,"region":"Riyadh Province","street_name":null,"longitude":45.2431035}

//        String city = jsonObjectRuntime.get("city") == null ? "not found" : jsonObjectRuntime.get("city").toString();
//        String country = jsonObjectRuntime.get("country") == null ? "not found" : jsonObjectRuntime.get("country").toString();
//        String streetName = jsonObjectRuntime.get("street_name") == null ? "not found" : jsonObjectRuntime.get("street_name").toString();
//        String number = jsonObjectRuntime.get("street_number") == null ? "not found " : jsonObjectRuntime.get("street_number").toString();


        return response;

       /* Google
       String url = "https://maps.googleapis.com/maps/api/geocode/json?latlng="+ latitude +","+longitude+
                "&sensor=false&key=" + APIKeys.getGoogleKey();
        String ret = Rest.httpsGet(url);
        //return (JSONObject) new JSONParser().parse(ret);
        //TODO hack. need better parsing later
        JSONObject obj = (JSONObject) new JSONParser().parse(ret);
        JSONArray array = (JSONArray) obj.get("results");
        JSONObject object = (JSONObject) array.get(0);
        String addr = (String) object.get("formatted_address");
        StringTokenizer stringTokenizer = new StringTokenizer(addr, ",");
        String street = stringTokenizer.nextToken().trim();
        String city = stringTokenizer.nextToken().trim();
        String country = stringTokenizer.nextToken().trim();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("street", street);
        jsonObject.put("city", city);
        jsonObject.put("country", country);
        return jsonObject;               */

    }

}
