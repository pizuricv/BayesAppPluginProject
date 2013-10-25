package com.ai.myplugin.util;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by User: veselin
 * On Date: 25/10/13
 */
public class TwitterConfig {
    private static final String CONSUMER_KEY = "OAuthConsumerKey";
    private static final String CONSUMER_SECRET = "OAuthConsumerSecret";
    private static final String ACCESS_TOKEN = "OAuthAccessToken";
    private static final String ACCESS_TOKEN_SECRET = "OAuthAccessTokenSecret";
    private static String CONFIG_FILE = "bn.properties";
    private static ConfigurationBuilder cb = null;

    static {
        cb = new ConfigurationBuilder();
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(CONFIG_FILE));
        } catch (IOException e) {
            e.printStackTrace();
        }
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey((String) properties.get(CONSUMER_KEY))
                .setOAuthConsumerSecret((String) properties.get(CONSUMER_SECRET))
                .setOAuthAccessToken((String) properties.get(ACCESS_TOKEN))
                .setOAuthAccessTokenSecret((String) properties.get(ACCESS_TOKEN_SECRET));

    }

    public static ConfigurationBuilder getTwitterConfigurationBuilder() {
        return cb;
    }
}
