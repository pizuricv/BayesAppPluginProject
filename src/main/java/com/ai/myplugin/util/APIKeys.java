/**
 * Created with IntelliJ IDEA.
 * User: pizuricv
 */

package com.ai.myplugin.util;

import java.util.Properties;

public final class APIKeys {
    static String mashapeKey;
    static String googleKey;
    static String sensulosKey;

    static {
       Properties properties = Config.load();
       mashapeKey = (String) properties.get("mashapeKey");
       googleKey = (String) properties.get("googleKey");
       sensulosKey = (String) properties.get("sensulosKey");
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
