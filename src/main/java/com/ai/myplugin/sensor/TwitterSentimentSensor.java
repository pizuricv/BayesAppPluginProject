package com.ai.myplugin.sensor;

import com.ai.bayes.plugins.BNSensorPlugin;
import com.ai.bayes.scenario.TestResult;
import com.ai.myplugin.util.SentimentAnalysis;
import com.ai.myplugin.util.SlidingWindowCounter;
import com.ai.myplugin.util.TwitterConfig;
import com.ai.myplugin.util.Utils;
import com.ai.util.resource.TestSessionContext;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import twitter4j.*;
import twitter4j.TwitterException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by User: veselin
 * On Date: 25/10/13
 */
@PluginImplementation
public class TwitterSentimentSensor implements BNSensorPlugin{
    private static final Log log = LogFactory.getLog(TwitterSentimentSensor.class);
    private static final String SEARCH_TERMS = "search_terms";
    private static final String WINDOW = "window";
    private int window = 15;
    Map<String, Object> propertiesMap = new ConcurrentHashMap<String, Object>();
    private SlidingWindowCounter counterPositive = new SlidingWindowCounter(15, "positive sentiment");
    private SlidingWindowCounter counterNegative = new SlidingWindowCounter(15, "negative sentiment");
    private SlidingWindowCounter mentions = new SlidingWindowCounter(15, "mentions");
    private boolean running = false;
    TwitterStream twitterStream = new TwitterStreamFactory(TwitterConfig.getTwitterConfigurationBuilder()).getInstance();

    @Override
    public String[] getRequiredProperties() {
        return new String []{SEARCH_TERMS, WINDOW};
    }

    @Override
    public String[] getRuntimeProperties() {
        return new String[]{};
    }

    public void setProperty(String string, Object obj) {
        if(Arrays.asList(getRequiredProperties()).contains(string)) {
            if(string.equalsIgnoreCase(WINDOW))
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
        log.info("execute " + getName() + ", sensor type:" + this.getClass().getName());

        if(!running){
            int w = getProperty(WINDOW) != null? ((Double)getProperty(WINDOW)).intValue(): window;
            counterPositive.setSlidingWindowMinutes(w);
            counterNegative.setSlidingWindowMinutes(w);
            mentions.setSlidingWindowMinutes(w);
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
                if(counterPositive.getTotalCount() > counterNegative.getTotalCount())
                    return "Positive";
                else if(counterPositive.getTotalCount() == counterNegative.getTotalCount())
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
                jsonObject.put("positiveCounter", counterPositive.getTotalCount());
                jsonObject.put("negativeCounter", counterNegative.getTotalCount());
                jsonObject.put("mentions", mentions.getTotalCount());
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

    public synchronized void runSentiment(final String searchTerms){
        FilterQuery filterQuery = new FilterQuery();
        final String [] searchSep = searchTerms.split(SentimentAnalysis.SEPARATOR);

        StatusListener listener = new StatusListener() {
            public void onStatus(Status status) {
                //TODO this is a way to make the search logical AND, rather than logical OR, need better way to do it
                if (SentimentAnalysis.isMatching(searchTerms, status.getText())) {
                    log.debug("********* Found *************");
                    //log.debug("User is : " + status.getUser().getName());
                    //log.debug("Text is : " + status.getText());
                    mentions.incrementAndGet();
                    int num = SentimentAnalysis.sentimentForString(status.getText().toLowerCase());
                    if(num == 1)
                        counterPositive.incrementAndGet();
                    else if(num == -1)
                        counterNegative.incrementAndGet();
                    //log.debug("Counter positive is: " + counterPositive.getTotalCount());
                    //log.debug("Counter negative is: " + counterNegative.getTotalCount());
                }
            }
            public void onDeletionNotice(
                    StatusDeletionNotice statusDeletionNotice) {
                log.debug("Got a status deletion notice id:"
                                + statusDeletionNotice.getStatusId());
            }
            public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
                log.warn("Got track limitation notice:"
                        + numberOfLimitedStatuses);
            }
            public void onScrubGeo(long userId, long upToStatusId) {
                log.debug("Got scrub_geo event userId:" + userId
                        + " upToStatusId:" + upToStatusId);
            }

            @Override
            public void onStallWarning(StallWarning stallWarning) {
                log.warn("Got StallWarning: " + stallWarning.getMessage());
            }

            public void onException(Exception ex) {
                log.error(ex.getLocalizedMessage());
                ex.printStackTrace();
            }
        };
        twitterStream.addListener(listener);
        //twitterStream.sample();  //this is the way to get 1% of the stream, below we use the filter instead
        filterQuery.track(searchSep);
        twitterStream.filter(filterQuery);
        running = true;
    }

    @Override
    public void shutdown(TestSessionContext testSessionContext) {
        log.debug("Shutdown : " + getName() + ", sensor : "+this.getClass().getName());
        running = false;
        counterNegative.reset();
        counterPositive.reset();
        mentions.reset();
        twitterStream.shutdown();
    }

    public static void main(String[] args) throws TwitterException, IOException {
        final TwitterSentimentSensor twitterSentimentSensor = new TwitterSentimentSensor();
        twitterSentimentSensor.setProperty(WINDOW, 3);
        twitterSentimentSensor.setProperty(SEARCH_TERMS, "justin bieber");
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while(true){
                    try {
                        Thread.sleep(10000);
                        log.info("execute...");
                        TestResult testResult = twitterSentimentSensor.execute(null);
                        log.info("RAW data: " +testResult.getRawData());
                        log.info("Observed state: " + testResult.getObserverState());
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
