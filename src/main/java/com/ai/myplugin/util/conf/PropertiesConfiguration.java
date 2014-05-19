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
        return getStringOpt(key).orElseThrow(() -> new RuntimeException("Configuration missing: " + key));
    }

    @Override
    public String getNonEmptyString(String key) {
        return getNonEmptyStringOpt(key).orElseThrow(() -> new RuntimeException("Configuration missing: " + key));
    }

    @Override
    public Optional<String> getStringOpt(String key) {
        return Optional.ofNullable(properties.getProperty(key));
    }

    @Override
    public Optional<String> getNonEmptyStringOpt(String key) {
        return getStringOpt(key).filter(val -> !val.trim().isEmpty());
    }
}
