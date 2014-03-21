/**
 * Created with IntelliJ IDEA.
 * User: pizuricv
 */

package com.ai.myplugin.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class APIKeys {
    static String mashapeKey;
    static String googleKey;
    static String sensulosKey;
    private static final Log log = LogFactory.getLog(APIKeys.class);

    static {
       String CONFIG_FILE = "bn.properties";
       Properties properties = new Properties();
       try {
           properties.load(new FileInputStream(CONFIG_FILE));
           mashapeKey = (String) properties.get("mashapeKey");
           googleKey = (String) properties.get("googleKey");
           sensulosKey = (String) properties.get("sensulosKey");
       } catch (IOException e) {
           e.printStackTrace();
           log.error(e.getMessage());
       }
   }

    public static String getMashapeKey(){
        return mashapeKey;
    }

    public static String getGoogleKey(){
        return googleKey;
    }

    public static String getSensulosKey() {
        return sensulosKey;
    }
}
