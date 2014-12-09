package com.ai.myplugin.action;

import com.ai.api.*;
import com.ai.myplugin.util.conf.Config;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.DirectMessage;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@PluginImplementation
@PluginHeader(version = "1.0.1", author = "Veselin", category = "Java Script", iconURL = "http://app.waylay.io/icons/twitter.png")
public class TwitterDMAction implements ActuatorPlugin {

    private static final Logger log = LoggerFactory.getLogger(TwitterDMAction.class);

    private final String CONSUMER_KEY = "OAuthConsumerKey";
    private final String CONSUMER_SECRET = "OAuthConsumerSecret";
    private final String ACCESS_TOKEN = "OAuthAccessToken";
    private final String ACCESS_TOKEN_SECRET = "OAuthAccessTokenSecret";
    private final String TWITTER_ACCOUNT = "twitter account";
    private final String TWITTER_MESSAGE = "twitter message";

    private static final String NAME = "Twitter";

    Map<String, Object> propertiesMap = new ConcurrentHashMap<String, Object>();

    @Override
    public Map<String,PropertyType> getRequiredProperties() {
        Map<String,PropertyType> map = new HashMap<>();
        //normally these settings are on the server side
        map.put(ACCESS_TOKEN, new PropertyType(DataType.STRING, false, true));
        map.put(ACCESS_TOKEN_SECRET, new PropertyType(DataType.STRING, false, true));
        map.put(CONSUMER_KEY, new PropertyType(DataType.STRING, false, true));
        map.put(CONSUMER_SECRET, new PropertyType(DataType.STRING, false, true));
        map.put(TWITTER_ACCOUNT, new PropertyType(DataType.STRING, true, true));
        map.put(TWITTER_MESSAGE, new PropertyType(DataType.STRING, true, true));
        return map;
    }

    @Override
    public void setProperty(String string, Object o) {
        if(CONSUMER_KEY.equals(string) || CONSUMER_SECRET.equals(string) ||
                ACCESS_TOKEN.equals(string) || ACCESS_TOKEN_SECRET.equals(string) ||
                TWITTER_ACCOUNT.equals(string) || TWITTER_MESSAGE.equals(string)) {
            propertiesMap.put(string, o.toString());
        } else {
            throw new RuntimeException("Property "+ string + " not in the required settings");
        }
    }

    @Override
    public Object getProperty(String string) {
        return propertiesMap.get(string);
    }

    @Override
    public String getDescription() {
        return "Twitter action";
    }

    @Override
    public ActuatorResult action(SessionContext testSessionContext) {
        ConfigurationBuilder cb = new ConfigurationBuilder();

        fetchTwitterPropertiesFromFile();

        cb.setDebugEnabled(true)
                .setOAuthConsumerKey((String) getProperty(CONSUMER_KEY))
                .setOAuthConsumerSecret((String) getProperty(CONSUMER_SECRET))
                .setOAuthAccessToken((String) getProperty(ACCESS_TOKEN))
                .setOAuthAccessTokenSecret((String) getProperty(ACCESS_TOKEN_SECRET));
        TwitterFactory tf = new TwitterFactory(cb.build());
        Twitter twitter = tf.getInstance();
        String twitterMessage = getProperty(TWITTER_MESSAGE) + " , on " + (new Date()).toString();
        Map map = (Map) testSessionContext.getAttribute(SessionParams.RAW_DATA);
        try {
            twitterMessage = testSessionContext.parseTemplateFromRawMap(twitterMessage, map);
        }catch (Exception e){
            log.warn(e.getLocalizedMessage());
        }

        try {
            DirectMessage message = twitter.sendDirectMessage((String) getProperty(TWITTER_ACCOUNT), twitterMessage);
            log.info("Direct message successfully sent to " + message.getRecipientScreenName());
        } catch (TwitterException te) {
            log.error("Failed to send a direct message: " + te.getMessage(), te);
            return new ActuatorFailedResult(te.getMessage());
        }
        return ActuatorSuccessResult.INSTANCE;
    }

    @Override
    public String getName() {
        return NAME;
    }


    //in case that the file exist, use these properties
    private void fetchTwitterPropertiesFromFile(){
        com.ai.myplugin.util.conf.Configuration config = Config.load();
        config.getStringOpt(CONSUMER_KEY).ifPresent(value -> propertiesMap.put(CONSUMER_KEY, value));
        config.getStringOpt(CONSUMER_SECRET).ifPresent(value -> propertiesMap.put(CONSUMER_SECRET, value));
        config.getStringOpt(ACCESS_TOKEN).ifPresent(value -> propertiesMap.put(ACCESS_TOKEN, value));
        config.getStringOpt(ACCESS_TOKEN_SECRET).ifPresent(value -> propertiesMap.put(ACCESS_TOKEN_SECRET, value));
    }

//    public static void main(String[] args) {
//        /*ConfigurationBuilder cb = new ConfigurationBuilder();
//        cb.setDebugEnabled(true)
//                .setOAuthConsumerKey("******************")
//                .setOAuthConsumerSecret("******************")
//                .setOAuthAccessToken("******************")
//                .setOAuthAccessTokenSecret("******************");
//        TwitterFactory tf = new TwitterFactory(cb.build());
//        Twitter twitter = tf.getInstance();
//
//        try {
//            DirectMessage message = twitter.sendDirectMessage("open3M", "test tweet");
//            System.out.println("Direct message successfully sent to " + message.getRecipientScreenName());
//            System.exit(0);
//        } catch (TwitterException te) {
//            System.out.println("Failed to send a direct message: " + te.getMessage());
//            System.exit(-1);
//        }*/
//        TwitterDMAction twitterDMAction = new TwitterDMAction();
//        twitterDMAction.setProperty("twitter account", "pizuricv");
//        twitterDMAction.setProperty("twitter message", "hello test node1->value1 ahh");
//
//        SessionContext testSessionContext = new SessionContext(1);
//        Map<String, Object> mapTestResult = new HashMap<String, Object>();
//        JSONObject objRaw = new JSONObject();
//        objRaw.put("value1", 1);
//        objRaw.put("time", 123);
//        objRaw.put("rawData", objRaw.toJSONString());
//        mapTestResult.put("node1", objRaw);
//
//        objRaw = new JSONObject();
//        objRaw.put("value2", 1);
//        objRaw.put("time", 213213);
//        objRaw.put("rawData", objRaw.toJSONString());
//        mapTestResult.put("node2", objRaw);
//
//        testSessionContext.setAttribute(SessionParams.RAW_DATA, mapTestResult);
//        twitterDMAction.action(testSessionContext);
//    }
}
