/**
 * User: pizuricv
 * Date: 6/4/13
 */
package com.ai.myplugin.action;

import com.ai.api.ActuatorPlugin;
import com.ai.api.ActuatorResult;
import com.ai.api.SessionContext;
import com.ai.api.SessionParams;
import com.ai.myplugin.util.conf.Config;
import com.ai.myplugin.util.RawDataParser;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import twitter4j.DirectMessage;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@PluginImplementation
public class TwitterDMAction implements ActuatorPlugin {
    private static final Log log = LogFactory.getLog(TwitterDMAction.class);

    private final String CONSUMER_KEY = "OAuthConsumerKey";
    private final String CONSUMER_SECRET = "OAuthConsumerSecret";
    private final String ACCESS_TOKEN = "OAuthAccessToken";
    private final String ACCESS_TOKEN_SECRET = "OAuthAccessTokenSecret";
    private final String TWITTER_ACCOUNT = "twitter account";
    private final String TWITTER_MESSAGE = "twitter message";

    private static final String NAME = "Twitter";

    Map<String, Object> propertiesMap = new ConcurrentHashMap<String, Object>();

    @Override
    public String[] getRequiredProperties() {
        return new String[] {CONSUMER_KEY, CONSUMER_SECRET, ACCESS_TOKEN, ACCESS_TOKEN_SECRET,
                TWITTER_ACCOUNT, TWITTER_MESSAGE};
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
        Configuration configuration;
        boolean success = true;

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
            twitterMessage = RawDataParser.parseTemplateFromRawMap(twitterMessage, map);
        }catch (Exception e){
            log.warn(e.getLocalizedMessage());
        }

        try {
            DirectMessage message = twitter.sendDirectMessage((String) getProperty(TWITTER_ACCOUNT),
                    twitterMessage);
            System.out.println("Direct message successfully sent to " + message.getRecipientScreenName());
        } catch (TwitterException te) {
            te.printStackTrace();
            System.out.println("Failed to send a direct message: " + te.getMessage());
            success = false;
        }
        final boolean finalSuccess = success;
        return new ActuatorResult() {
            @Override
            public boolean isSuccess() {
                return finalSuccess;
            }

            @Override
            public String getObserverState() {
                return null;
            }
        };
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

    public static void main(String[] args) {
        /*ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey("******************")
                .setOAuthConsumerSecret("******************")
                .setOAuthAccessToken("******************")
                .setOAuthAccessTokenSecret("******************");
        TwitterFactory tf = new TwitterFactory(cb.build());
        Twitter twitter = tf.getInstance();

        try {
            DirectMessage message = twitter.sendDirectMessage("open3M", "test tweet");
            System.out.println("Direct message successfully sent to " + message.getRecipientScreenName());
            System.exit(0);
        } catch (TwitterException te) {
            te.printStackTrace();
            System.out.println("Failed to send a direct message: " + te.getMessage());
            System.exit(-1);
        }*/
        TwitterDMAction twitterDMAction = new TwitterDMAction();
        twitterDMAction.setProperty("twitter account", "pizuricv");
        twitterDMAction.setProperty("twitter message", "hello test node1->value1 ahh");

        SessionContext testSessionContext = new SessionContext(1);
        Map<String, Object> mapTestResult = new HashMap<String, Object>();
        JSONObject objRaw = new JSONObject();
        objRaw.put("value1", 1);
        objRaw.put("time", 123);
        objRaw.put("rawData", objRaw.toJSONString());
        mapTestResult.put("node1", objRaw);

        objRaw = new JSONObject();
        objRaw.put("value2", 1);
        objRaw.put("time", 213213);
        objRaw.put("rawData", objRaw.toJSONString());
        mapTestResult.put("node2", objRaw);

        testSessionContext.setAttribute(SessionParams.RAW_DATA, mapTestResult);
        twitterDMAction.action(testSessionContext);
    }
}
