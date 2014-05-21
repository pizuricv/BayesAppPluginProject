/**
 * Created with IntelliJ IDEA.
 * User: pizuricv
 */

package com.ai.myplugin.util;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Rest {
    private static final Log log = LogFactory.getLog(Rest.class);

    private static final int TIMEOUT_MILLIS = (int)TimeUnit.SECONDS.toMillis(300);

    public static RestReponse httpGet(String urlPath, Map<String, String> httpSettings) throws IOException {
        log.info("GET " + urlPath);

        URL url;
        try {
            url = new URL(urlPath);
        } catch (MalformedURLException e) {
            throw new IOException(e);
        }

        long start = System.currentTimeMillis();
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) url.openConnection();
            for(Map.Entry<String, String> entry : httpSettings.entrySet()){
                conn.addRequestProperty(entry.getKey(), entry.getValue());
            }
            conn.setConnectTimeout(TIMEOUT_MILLIS);
            conn.setReadTimeout(TIMEOUT_MILLIS);
            conn.setRequestMethod("GET");

            String body = executeAndReturnBodyAsString(conn);
            long elapsed = System.currentTimeMillis() - start;
            log.info("GET " + urlPath + " in " + elapsed + " ms");
            return new RestReponse(body);
        } finally {
            if(conn != null) {
                conn.disconnect();
            }
        }
    }

    public static RestReponse httpGet(String urlPath) throws IOException {
        return httpGet(urlPath, Collections.emptyMap());
    }


    private static String executeAndReturnBodyAsString(HttpURLConnection conn) throws IOException {
        try(
                InputStream is = conn.getInputStream();
                InputStreamReader reader = new InputStreamReader(is);
                BufferedReader rd = new BufferedReader(reader)
        ){
            return readToString(rd);
        }
    }

    private static String readToString(BufferedReader rd) throws IOException {
        String body;
        String inputLine;
        StringBuilder stringBuilder = new StringBuilder();
        while ((inputLine = rd.readLine()) != null){
            stringBuilder.append(inputLine);
        }
        body = stringBuilder.toString();
        return body;
    }

    public static class RestReponse{
        // TODO we could also collect the failures in this class
        private final String body;

        private RestReponse(String body) {
            this.body = body;
        }

        public String body(){
            return body;
        }

        public JSONObject json() throws ParseException {
            return (JSONObject) new JSONParser().parse(body);
        }

        public JSONArray jsonArray() throws ParseException {
            return (JSONArray) new JSONParser().parse(body);
        }
    }



}
