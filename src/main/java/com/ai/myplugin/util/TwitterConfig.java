package com.ai.myplugin.util;

import com.ai.myplugin.util.conf.Config;

import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

import java.util.Properties;

/**
 * Created by User: veselin
 * On Date: 25/10/13
 */
public class TwitterConfig {

    private static final String CONSUMER_KEY = "OAuthConsumerKey";
    private static final String CONSUMER_SECRET = "OAuthConsumerSecret";
    private static final String ACCESS_TOKEN = "OAuthAccessToken";
    private static final String ACCESS_TOKEN_SECRET = "OAuthAccessTokenSecret";

    private static Configuration configuration = buildTwitterConfig();

    public static Configuration getTwitterConfigurationBuilder() {
        return configuration;
    }

    private static Configuration buildTwitterConfig(){
        ConfigurationBuilder cb = new ConfigurationBuilder();
        com.ai.myplugin.util.conf.Configuration config = Config.load();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey(config.getString(CONSUMER_KEY))
                .setOAuthConsumerSecret(config.getString(CONSUMER_SECRET))
                .setOAuthAccessToken(config.getString(ACCESS_TOKEN))
                .setOAuthAccessTokenSecret(config.getString(ACCESS_TOKEN_SECRET));
        return cb.build();
    }
}
