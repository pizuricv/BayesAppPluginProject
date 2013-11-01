/**
 * Created with IntelliJ IDEA.
 * User: pizuricv
 */

package com.ai.myplugin.util;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Map;

public class Rest {
    private static final Log log = LogFactory.getLog(Rest.class);
    public static String httpGet(String urlPath, Map<String, String> httpSettings) throws Exception {
        URL url;

        try {
            url = new URL(urlPath);
        } catch (MalformedURLException e) {
            log.error(e.getLocalizedMessage());
            throw new Exception(e);
        }
        HttpURLConnection conn;
        try {
            conn = (HttpURLConnection) url.openConnection();
            if(httpSettings != null){
                for(Map.Entry<String, String> entry : httpSettings.entrySet()){
                    conn.addRequestProperty(entry.getKey(), entry.getValue());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            log.error(e.getLocalizedMessage());
            throw new Exception(e);
        }
        assert conn != null;
        try {
            conn.setRequestMethod("GET");
        } catch (ProtocolException e) {
            log.error(e.getLocalizedMessage());
            throw new Exception(e);
        }

        BufferedReader rd = null;
        try {
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        } catch (IOException e) {
            log.error(e.getLocalizedMessage());
            throw new Exception(e);
        }
        String inputLine;
        StringBuffer stringBuffer = new StringBuffer();

        assert rd != null;
        try {
            while ((inputLine = rd.readLine()) != null){
                stringBuffer.append(inputLine);
            }
        } catch (IOException e) {
            log.error(e.getLocalizedMessage());
            throw new Exception(e);
        }
        conn.disconnect();
        try {
            rd.close();
        } catch (IOException e) {
            e.printStackTrace();
            log.error(e.getLocalizedMessage());
        }
        return stringBuffer.toString();
    }

    public static String httpGet(String urlPath) throws Exception {
        return httpGet(urlPath, null);
    }

}
