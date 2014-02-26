/**
 * Created with IntelliJ IDEA.
 * User: pizuricv
 */

package com.ai.myplugin.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class APIKeys {
    static String mashapeKey;
    static String googleKey;

    static {
       String CONFIG_FILE = "bn.properties";
       Properties properties = new Properties();
       try {
           properties.load(new FileInputStream(CONFIG_FILE));
           mashapeKey = (String) properties.get("mashapeKey");
           googleKey = (String) properties.get("googleKey");
       } catch (IOException e) {
           e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
       }
   }

    public static String getMashapeKey(){
        return mashapeKey;
    }

    public static String getGoogleKey(){
        return googleKey;
    }


}
