/**
 * Created by User: veselin
 * On Date: 25/10/13
 */

package com.ai.myplugin.sensor;

import com.ai.api.*;
import com.ai.myplugin.util.SentimentAnalysis;
import com.ai.myplugin.util.SlidingWindowTimeCounter;
import com.ai.myplugin.util.TwitterConfig;
import com.ai.myplugin.util.Utils;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.*;
import twitter4j.TwitterException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


@PluginImplementation
@PluginHeader(version = "1.0.1", author = "Veselin", category = "Social", iconURL = "http://app.waylay.io/icons/twitter_sentiment.png")
public class TwitterSentimentSensor implements SensorPlugin {

    private static final Logger log = LoggerFactory.getLogger(TwitterSentimentSensor.class);

    private static final String SEARCH_TERMS = "search_terms";
    private static final String WINDOW = "window";
    private int window = 15;
    Map<String, Object> propertiesMap = new ConcurrentHashMap<String, Object>();
    private SlidingWindowTimeCounter counterPositive = new SlidingWindowTimeCounter(15, "positive sentiment");
    private SlidingWindowTimeCounter counterNegative = new SlidingWindowTimeCounter(15, "negative sentiment");
    private SlidingWindowTimeCounter mentions = new SlidingWindowTimeCounter(15, "mentions");
    private boolean running = false;
    TwitterStream twitterStream = new TwitterStreamFactory(TwitterConfig.getTwitterConfigurationBuilder()).getInstance();
    private String[] states = {"Positive", "Neutral", "Negative"};

    @Override
    public Map<String, PropertyType> getRequiredProperties() {
        Map<String, PropertyType> map = new HashMap<>();
        map.put(SEARCH_TERMS, new PropertyType(DataType.STRING, true, false));
        map.put(WINDOW, new PropertyType(DataType.STRING, true, false));
        return map;
    }

    @Override
    public Map<String, PropertyType> getRuntimeProperties() {
        return new HashMap<>();
    }

    public void setProperty(String string, Object obj) {
        if(getRequiredProperties().keySet().contains(string)) {
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
    public SensorResult execute(SessionContext testSessionContext) {
        log.info("execute " + getName() + ", sensor type:" + this.getClass().getName());

        if(!running){
            int w = getProperty(WINDOW) != null? ((Double)getProperty(WINDOW)).intValue(): window;
            counterPositive.setSlidingWindowMinutes(w);
            counterNegative.setSlidingWindowMinutes(w);
            mentions.setSlidingWindowMinutes(w);
            runSentiment((String) getProperty(SEARCH_TERMS));
        }
        return new SensorResult() {
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
    public Set<String> getSupportedStates() {
        return new HashSet(Arrays.asList(states));
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
    public void setup(SessionContext testSessionContext) {
        log.debug("Setup : " + getName() + ", sensor : "+this.getClass().getName());
    }

    @Override
    public void shutdown(SessionContext testSessionContext) {
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
                        SensorResult testResult = twitterSentimentSensor.execute(null);
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
