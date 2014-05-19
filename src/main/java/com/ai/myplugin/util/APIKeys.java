/**
 * Created with IntelliJ IDEA.
 * User: pizuricv
 */

package com.ai.myplugin.util;

import com.ai.myplugin.util.conf.Config;
import com.ai.myplugin.util.conf.Configuration;

public final class APIKeys {

    private static final Configuration config = Config.load();

    public static String getMashapeKey(){
        return config.getNonEmptyString("mashapeKey");
    }

    public static String getGoogleKey(){
        return config.getNonEmptyString("googleKey");
    }

    public static String getSensulosKey() {
        return config.getNonEmptyString("sensulosKey");
    }
}
