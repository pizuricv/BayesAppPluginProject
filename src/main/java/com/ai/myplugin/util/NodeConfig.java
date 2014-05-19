package com.ai.myplugin.util;

import com.ai.myplugin.util.conf.Config;
import com.ai.myplugin.util.conf.Configuration;

import java.io.File;

public final class NodeConfig {

    private static final Configuration config = Config.load();

    private static String defaultNodePath = defaultNodePath();

    public static String getNodePath(){
        return config.getStringOpt("nodePath").orElse(defaultNodePath);
    }

    public static String getNodeDir(){
        return config.getStringOpt("nodeDir").orElse("/var/tmp");
    }

    private static String defaultNodePath() {
        File node = new File("/usr/local/bin/node");
        if(!node.exists()){
            node = new File("/opt/local/bin/node");
        }
        if(node.exists()) {
            return node.getAbsolutePath();
        }else{
            // just get it from the path
            return "node";
        }
    }
}
