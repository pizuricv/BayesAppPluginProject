package com.ai.myplugin.sensor;

import com.ai.bayes.plugins.BNSensorPlugin;
import com.ai.bayes.scenario.TestResult;
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
public class TwitterStreamSearchSensor implements BNSensorPlugin{
    private static final Log log = LogFactory.getLog(TwitterStreamSearchSensor.class);
    private static final String SEARCH_TERMS = "search_terms";
    private int window = 15;
    Map<String, Object> propertiesMap = new ConcurrentHashMap<String, Object>();
    private boolean running = false;
    private List<String> listFoundItems = new ArrayList();
    TwitterStream twitterStream = new TwitterStreamFactory(TwitterConfig.getTwitterConfigurationBuilder()).getInstance();

    @Override
    public String[] getRequiredProperties() {
        return new String []{SEARCH_TERMS};
    }

    public void setProperty(String string, Object obj) {
        if(Arrays.asList(getRequiredProperties()).contains(string)) {
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
        return "TwitterStreamSearchSensor";
    }

    @Override
    public TestResult execute(TestSessionContext testSessionContext) {
        log.info("execute " + getName() + ", sensor type:" + this.getClass().getName());
        listFoundItems.clear();
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
                return "TwitterQuerySensor result";
            }

            @Override
            public String getObserverState() {
                if(listFoundItems.size() == 0)
                    return "Not found";
                return "Found";
            }

            @Override
            public List<Map<String, Number>> getObserverStates() {
                return null;
            }

            @Override
            public String getRawData() {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("tweets", listFoundItems);
                return jsonObject.toJSONString();
            }
        };
    }

    @Override
    public String getName() {
        return "TwitterStreamSearchSensor";
    }

    @Override
    public String[] getSupportedStates() {
        return new String[] {"Found", "Not Found"};
    }

    public synchronized void runSentiment(String searchTerms){
        final String [] searchSep = searchTerms.split(";");
        StatusListener listener = new StatusListener() {
            public void onStatus(Status status) {
                for (String token: searchSep){
                    if (status.getText().toLowerCase().indexOf(token.toLowerCase()) > 0) {
                        log.debug("********* Found *************");
                        //log.debug("User is : " + status.getUser().getName());
                        //log.debug("Text is : " + status.getText());
                        listFoundItems.add(status.getText());
                    }
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
        twitterStream.sample();
        running = true;
    }

    @Override
    public void shutdown(TestSessionContext testSessionContext) {
        log.debug("Shutdown : " + getName() + ", sensor : "+this.getClass().getName());
        running = false;
        twitterStream.shutdown();
    }

    public static void main(String[] args) throws TwitterException, IOException {
        final TwitterStreamSearchSensor twitterSentimentSensor = new TwitterStreamSearchSensor();
        twitterSentimentSensor.setProperty(SEARCH_TERMS, "justin;bieber");
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
