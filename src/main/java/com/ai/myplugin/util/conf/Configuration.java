package com.ai.myplugin.util.conf;


import java.util.Optional;

public interface Configuration {

    public String getString(String key);

    public String getNonEmptyString(String key);

    public Optional<String> getStringOpt(String key);

    public Optional<String> getNonEmptyStringOpt(String key);

}
