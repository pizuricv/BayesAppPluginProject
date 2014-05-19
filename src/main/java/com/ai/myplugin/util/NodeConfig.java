package com.ai.myplugin.util;

import java.io.File;
import java.util.Properties;

public final class NodeConfig {

    private static String nodePath;
    private static String nodeDir;

    static {
        Properties properties = Config.load();
        nodePath = properties.getProperty("nodePath", defaultNodePath());
        nodeDir = properties.getProperty("nodeDir", "/var/tmp");
    }

    public static String getNodePath(){
        return nodePath;
    }

    public static String getNodeDir(){
        return nodeDir;
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
