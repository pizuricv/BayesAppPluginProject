/**
 * Created with IntelliJ IDEA.
 * User: pizuricv
 */

package com.ai.myplugin.util;

import com.ai.myplugin.util.io.IOUtil;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class Rest {
    private static final Logger log = LoggerFactory.getLogger(Rest.class);

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

            RestReponse response = getResponse(conn);
            long elapsed = System.currentTimeMillis() - start;
            log.info("GET " + urlPath + " in " + elapsed + " ms");
            return response;
        } finally {
            if(conn != null) {
                conn.disconnect();
            }
        }
    }

    public static RestReponse httpGet(String urlPath) throws IOException {
        return httpGet(urlPath, Collections.emptyMap());
    }


    public static RestReponse httpPost(String urlPath, final String query, String charset) throws IOException{
        log.info("POST " + urlPath);

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

            conn.setRequestMethod("POST");
            conn.setDoOutput(true); // Triggers POST.
            conn.setRequestProperty("Accept-Charset", charset);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            writePostBody(query, charset, conn);

            //Get Response
            RestReponse response = getResponse(conn);
            long elapsed = System.currentTimeMillis() - start;
            log.info("POST " + urlPath + " in " + elapsed + " ms");
            return response;
        } finally {
            if(conn != null) {
                conn.disconnect();
            }
        }
    }

    private static RestReponse getResponse(HttpURLConnection conn) throws IOException {
        String body;
        if(conn.getResponseCode() >= 400){
            InputStream err = conn.getErrorStream();
            if(err != null) {
                body = IOUtil.readToString(err, Optional.ofNullable(conn.getContentEncoding()));
                // TODO do we still need to fail here or should the caller check?
                throw new IOException("Got " + conn.getResponseCode() + " " + conn.getResponseMessage() + "\n" + body);
            }else{
                throw new IOException("Got " + conn.getResponseCode() + " " + conn.getResponseMessage() + " but no contents");
            }
        }else{
            body = IOUtil.readToString(conn.getInputStream(), Optional.ofNullable(conn.getContentEncoding()));
            return new RestReponse(conn.getResponseCode(), conn.getContentType(), body);
        }
    }

    private static void writePostBody(String query, String charset, HttpURLConnection conn) throws IOException {
        try(
                OutputStream output = conn.getOutputStream()
        ) {
            output.write(query.getBytes(charset));
            output.flush();
        }
    }

    public static class RestReponse{

        private final int status;

        private final String body;

        private final String contentType;

        private RestReponse(final int status, final String contentType, final String body) {
            this.status = status;
            this.contentType = contentType;
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
