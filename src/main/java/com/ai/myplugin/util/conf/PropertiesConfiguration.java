package com.ai.myplugin.util.conf;

import java.util.Optional;
import java.util.Properties;

public class PropertiesConfiguration implements Configuration {
    private final Properties properties;

    public PropertiesConfiguration(Properties properties){
        this.properties = properties;
    }

    @Override
    public String getString(String key) {
        String value = properties.getProperty(key);
        if(value == null){
            throw new RuntimeException("Configuration missing: " + key);
        }
        return value;
    }

    @Override
    public Optional<String> getStringOpt(String key) {
        return Optional.ofNullable(properties.getProperty(key));
    }
}
