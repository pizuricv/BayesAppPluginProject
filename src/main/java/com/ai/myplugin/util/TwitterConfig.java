package com.ai.myplugin.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.Configuration;
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
    private static final Log log = LogFactory.getLog(TwitterConfig.class);
    private static final String CONSUMER_KEY = "OAuthConsumerKey";
    private static final String CONSUMER_SECRET = "OAuthConsumerSecret";
    private static final String ACCESS_TOKEN = "OAuthAccessToken";
    private static final String ACCESS_TOKEN_SECRET = "OAuthAccessTokenSecret";
    private static Configuration configuration = null;

    static {
        ConfigurationBuilder cb = new ConfigurationBuilder();
        Properties properties = Config.load();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey((String) properties.get(CONSUMER_KEY))
                .setOAuthConsumerSecret((String) properties.get(CONSUMER_SECRET))
                .setOAuthAccessToken((String) properties.get(ACCESS_TOKEN))
                .setOAuthAccessTokenSecret((String) properties.get(ACCESS_TOKEN_SECRET));
        configuration = cb.build();

    }

    public static Configuration getTwitterConfigurationBuilder() {
        return configuration;
    }
}
