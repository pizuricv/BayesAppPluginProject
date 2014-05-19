package com.ai.myplugin.util.conf;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class Config {

    private static final Log log = LogFactory.getLog(Config.class);

    private static final String CONFIG_FILE = "bn.properties";

    public static Configuration load(){
        Properties properties = new Properties();
        try(InputStream is = new FileInputStream(findConfigFile())) {
            properties.load(is);
        } catch (IOException e) {
            log.error(e.getLocalizedMessage(), e);
        }
        // TODO we might want to cache this and return a defensive copy?
        return new PropertiesConfiguration(properties);
    }

    // TODO we might want to also load it from the classpath
    public static File findConfigFile(){
        File file = new File(CONFIG_FILE);
        if(!file.exists()){
            file = new File(System.getProperty("user.home"), CONFIG_FILE);
        }
        return file;
    }
}
