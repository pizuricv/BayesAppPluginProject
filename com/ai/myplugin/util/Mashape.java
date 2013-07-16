/**
 * Created with IntelliJ IDEA.
 * User: pizuricv
 */

package com.ai.myplugin.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Mashape {
    static String key;
    private static final Object MASHAPE_KEY = "mashapeKey";

    static {
       String CONFIG_FILE = "bn.properties";
       Properties properties = new Properties();
       try {
           properties.load(new FileInputStream(CONFIG_FILE));
           key = (String) properties.get(MASHAPE_KEY);
       } catch (IOException e) {
           e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
       }
   }

    public static String getKey(){
        return key;
    }


}
