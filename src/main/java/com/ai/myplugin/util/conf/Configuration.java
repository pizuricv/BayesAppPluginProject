package com.ai.myplugin.util.conf;


import java.util.Optional;

public interface Configuration {

    String getString(String key);

    String getNonEmptyString(String key);

    Optional<String> getStringOpt(String key);

    Optional<String> getNonEmptyStringOpt(String key);

}
