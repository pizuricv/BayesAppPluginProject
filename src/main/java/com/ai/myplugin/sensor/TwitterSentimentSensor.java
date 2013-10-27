package com.ai.myplugin.sensor;

import com.ai.bayes.plugins.BNSensorPlugin;
import com.ai.bayes.scenario.TestResult;
import com.ai.myplugin.util.TwitterConfig;
import com.ai.myplugin.util.Utils;
import com.ai.util.resource.TestSessionContext;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import org.json.simple.JSONObject;
import twitter4j.*;
import twitter4j.TwitterException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by User: veselin
 * On Date: 25/10/13
 */
@PluginImplementation
public class TwitterSentimentSensor implements BNSensorPlugin{
    private static final String SEARCH_TERMS = "search_terms";
    private static final String BASELINE = "baseline";
    Map<String, Object> propertiesMap = new ConcurrentHashMap<String, Object>();
    //TODO add real analysis!!!
    private AtomicInteger counterPositive = new AtomicInteger(0);
    private AtomicInteger counterNegative = new AtomicInteger(0);
    private boolean running = false;
    private String [] positiveTerms = new String[] {"great", "super", "awesome", "nice", "lol", "cute", "happy", "good", "love"};
    private String [] negativeTerms = new String[] {"bad", "ugly", "fuck", "sad", "shit", "nasty"};
    TwitterStream twitterStream = new TwitterStreamFactory(TwitterConfig.getTwitterConfigurationBuilder().build()).getInstance();




    @Override
    public String[] getRequiredProperties() {
        return new String []{SEARCH_TERMS, BASELINE};
    }

    public void setProperty(String string, Object obj) {
        if(Arrays.asList(getRequiredProperties()).contains(string)) {
            if(string.equalsIgnoreCase(BASELINE))
                obj = Utils.getDouble(obj);
            propertiesMap.put(string, obj);
        } else {
            throw new RuntimeException("Property "+ string + " not in the required settings");
        }
    }

    public Object getProperty(String string) {
        return propertiesMap.get(string);
    }

    @Override
    public String getDescription() {
        return "TwitterSentimentSensor";
    }

    @Override
    public TestResult execute(TestSessionContext testSessionContext) {
        System.out.println("execute "+ getName() + ", sensor type:" +this.getClass().getName());

        if(!running){
            runSentiment((String) getProperty(SEARCH_TERMS));
        }
        return new TestResult() {
            @Override
            public boolean isSuccess() {
                return true;
            }

            @Override
            public String getName() {
                return "Twitter Sentiment";
            }

            @Override
            public String getObserverState() {
                if(counterPositive.intValue() > counterNegative.intValue())
                    return "Positive";
                if(counterPositive.intValue() == counterNegative.intValue())
                    return "Neutral";
                else
                    return "Negative";
            }

            @Override
            public List<Map<String, Number>> getObserverStates() {
                return null;
            }

            @Override
            public String getRawData() {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("positiveCounter", counterPositive.intValue());
                jsonObject.put("negativeCounter", counterNegative.intValue());
                return jsonObject.toJSONString();
            }
        };
    }

    @Override
    public String getName() {
        return "TwitterSentimentSensor";
    }

    @Override
    public String[] getSupportedStates() {
        return new String[] {"Positive", "Neutral", "Negative"};
    }

    private void weightSearchTerm(String searchItem){
        for(String positiveString : positiveTerms){
            if(searchItem.indexOf(positiveString) > -1)
                counterPositive.incrementAndGet();
        }
        for(String negativeString : negativeTerms){
            if(searchItem.indexOf(negativeString) > -1)
                counterNegative.incrementAndGet();
        }
    }

    public synchronized void runSentiment(String searchTerms){
        final String [] searchSep = searchTerms.split(";");

        StatusListener listener = new StatusListener() {
            public void onStatus(Status status) {
                for (String token: searchSep){
                    if (status.getText().toLowerCase().indexOf(token.toLowerCase()) > 0) {
                        System.out.println("********* Found *************");
                        System.out.println("User is : " + status.getUser().getName());
                        System.out.println("Text is : " + status.getText());
                        weightSearchTerm(status.getText());
                        System.out.println("Counter positive is: " + counterPositive.intValue());
                        System.out.println("Counter negative is: " + counterNegative.intValue());
                    }
                }
            }
            public void onDeletionNotice(
                    StatusDeletionNotice statusDeletionNotice) {
                //System.out.print(".");
                /*System.out.println("Got a status deletion notice id:"
                                + statusDeletionNotice.getStatusId());    */
            }
            public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
                System.out.println("Got track limitation notice:"
                        + numberOfLimitedStatuses);
            }
            public void onScrubGeo(long userId, long upToStatusId) {
                System.out.println("Got scrub_geo event userId:" + userId
                        + " upToStatusId:" + upToStatusId);
            }

            @Override
            public void onStallWarning(StallWarning stallWarning) {
                System.out.println("Got StallWarning: "+stallWarning.getMessage());
            }

            public void onException(Exception ex) {
                ex.printStackTrace();
                running = false;
                twitterStream.shutdown();
            }
        };
        twitterStream.addListener(listener);
        twitterStream.sample();
        running = true;
    }

    public static void main(String[] args) throws TwitterException, IOException {
        final TwitterSentimentSensor twitterSentimentSensor = new TwitterSentimentSensor();
        twitterSentimentSensor.setProperty(BASELINE, 0);
        twitterSentimentSensor.setProperty(SEARCH_TERMS, "justin;bieber");
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while(true){
                    try {
                        Thread.sleep(10000);
                        System.out.println("execute...");
                        TestResult testResult = twitterSentimentSensor.execute(null);
                        System.out.println(testResult.getRawData());
                        System.out.println(testResult.getObserverState());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        return;
                    }
                }
            }
        } ;
        runnable.run();


    }

}
