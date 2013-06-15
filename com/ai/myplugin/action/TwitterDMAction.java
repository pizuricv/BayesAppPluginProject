/**
 * User: pizuricv
 * Date: 6/4/13
 */
package com.ai.myplugin.action;

import com.ai.bayes.plugins.BNActionPlugin;
import com.ai.bayes.scenario.ActionResult;
import com.ai.util.resource.TestSessionContext;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import twitter4j.DirectMessage;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

@PluginImplementation
public class TwitterDMAction implements BNActionPlugin {

    private final String CONSUMER_KEY = "OAuthConsumerKey";
    private final String CONSUMER_SECRET = "OAuthConsumerSecret";
    private final String ACCESS_TOKEN = "OAuthAccessToken";
    private final String ACCESS_TOKEN_SECRET = "OAuthAccessTokenSecret";
    private final String TWITTER_ACCOUNT = "twitter account";
    private final String TWITTER_MESSAGE = "twitter message";

    private static final String NAME = "Twitter";

    Map<String, Object> propertiesMap = new ConcurrentHashMap<String, Object>();
    private static String CONFIG_FILE = "bn.properties";

    @Override
    public String[] getRequiredProperties() {
        return new String[] {CONSUMER_KEY, CONSUMER_SECRET, ACCESS_TOKEN, ACCESS_TOKEN_SECRET,
                TWITTER_ACCOUNT, TWITTER_MESSAGE};
    }

    //in case that the file exist, use these properties
    public void fetchTwitterPropertiesFromFile() throws IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream(CONFIG_FILE));
        propertiesMap.put(CONSUMER_KEY, properties.get(CONSUMER_KEY));
        propertiesMap.put(CONSUMER_SECRET, properties.get(CONSUMER_SECRET));
        propertiesMap.put(ACCESS_TOKEN, properties.get(ACCESS_TOKEN));
        propertiesMap.put(ACCESS_TOKEN_SECRET, properties.get(ACCESS_TOKEN_SECRET));
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
    public ActionResult action(TestSessionContext testSessionContext) {
        ConfigurationBuilder cb = new ConfigurationBuilder();
        boolean success = true;
        try {
            fetchTwitterPropertiesFromFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey((String) getProperty(CONSUMER_KEY))
                .setOAuthConsumerSecret((String) getProperty(CONSUMER_SECRET))
                .setOAuthAccessToken((String) getProperty(ACCESS_TOKEN))
                .setOAuthAccessTokenSecret((String) getProperty(ACCESS_TOKEN_SECRET));
        TwitterFactory tf = new TwitterFactory(cb.build());
        Twitter twitter = tf.getInstance();
        String twitterMessage = getProperty(TWITTER_MESSAGE) + " , on " + (new Date()).toString();

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
        return new ActionResult() {
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

    public static void main(String[] args) {
        ConfigurationBuilder cb = new ConfigurationBuilder();
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
        }
    }
}
