/**
 * User: pizuricv
 * Date: 6/4/13
 */
package com.ai.myplugin;

import com.ai.bayes.plugins.BNActionPlugin;
import com.ai.bayes.scenario.ActionResult;
import com.ai.util.resource.TestSessionContext;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import twitter4j.DirectMessage;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@PluginImplementation
public class TwitterDMAction implements BNActionPlugin {

    private final String CUSTOMER_KEY = "OAuthConsumerKey";
    private final String CUSTOMER_SECRET = "OAuthConsumerSecret";
    private final String ACCESS_TOKEN = "OAuthAccessToken";
    private final String ACCESS_TOKEN_SECRET = "OAuthAccessTokenSecret";
    private final String TWITTER_ACCOUNT = "twitter account";
    private final String TWITTER_MESSAGE = "twitter message";

    Map<String, Object> propertiesMap = new ConcurrentHashMap<String, Object>();

    @Override
    public String[] getRequiredProperties() {
        return new String[] {CUSTOMER_KEY, CUSTOMER_SECRET, ACCESS_TOKEN, ACCESS_TOKEN_SECRET,
                TWITTER_ACCOUNT, TWITTER_MESSAGE};
    }

    @Override
    public void setProperty(String string, Object o) {
        if(CUSTOMER_KEY.equals(string) || CUSTOMER_SECRET.equals(string) ||
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
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey((String) getProperty(CUSTOMER_KEY))
                .setOAuthConsumerSecret((String) getProperty(CUSTOMER_SECRET))
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
        return "Twitter Action";
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
