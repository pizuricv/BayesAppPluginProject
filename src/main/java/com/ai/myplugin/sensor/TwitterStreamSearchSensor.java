/**
 * Created by User: veselin
 * On Date: 25/10/13
 */

package com.ai.myplugin.sensor;

import com.ai.api.*;
import com.ai.myplugin.util.SentimentAnalysis;
import com.ai.myplugin.util.TwitterConfig;
import net.xeoh.plugins.base.annotations.PluginImplementation;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.*;
import twitter4j.TwitterException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;


@PluginImplementation
@PluginHeader(version = "1.0.1", author = "Veselin", category = "Social", iconURL = "http://app.waylay.io/icons/twitter_stream_sentiment.png")
public class TwitterStreamSearchSensor implements SensorPlugin {

    private static final Logger log = LoggerFactory.getLogger(TwitterStreamSearchSensor.class);

    private static final String SEARCH_TERMS = "search_terms";
    private static final String NAME = "TwitterStreamSearch";
    Map<String, Object> propertiesMap = new ConcurrentHashMap<String, Object>();
    private boolean running = false;
    private List<String> listFoundItems = Collections.synchronizedList(new ArrayList<String>());
    TwitterStream twitterStream = new TwitterStreamFactory(TwitterConfig.getTwitterConfigurationBuilder()).getInstance();
    AtomicBoolean atomicBoolean = new AtomicBoolean(false);
    protected ExecutorService cleanUpService = Executors.newSingleThreadExecutor();
    private String[] states = {"Found", "Not Found"};

    @Override
    public Map<String, PropertyType> getRequiredProperties() {
        Map<String, PropertyType> map = new HashMap<>();
        map.put(SEARCH_TERMS, new PropertyType(DataType.STRING, true, false));
        return map;
    }

    public void setProperty(String string, Object obj) {
        if(getRequiredProperties().keySet().contains(string)) {
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
        return "Twitter Stream Search Sensor, that next to found tweets for a given search item also returns sentiment result";
    }

    @Override
    public SensorResult execute(SessionContext testSessionContext) {
        log.info("execute " + getName() + ", sensor type:" + this.getClass().getName());
        atomicBoolean.set(true);
        if(!running){
            runSentiment((String) getProperty(SEARCH_TERMS));
            startCleanUpThread();
        }
        return new SensorResult() {
            @Override
            public boolean isSuccess() {
                return true;
            }

            @Override
            public String getObserverState() {
                if(listFoundItems.size() == 0)
                    return "Not Found";
                return "Found";
            }

            @Override
            public List<Map<String, Number>> getObserverStates() {
                return null;
            }

            @Override
            public String getRawData() {
                int positive = 0;
                int negative = 0;
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("tweets", listFoundItems);
                for(String item : listFoundItems){
                    int n = SentimentAnalysis.sentimentForString(item);
                    if(n > 0)
                        positive ++;
                    else if(n < 0)
                        negative ++;
                }
                jsonObject.put("positiveCounter", positive);
                jsonObject.put("negativeCounter", negative);
                jsonObject.put("mentions", listFoundItems.size());
                return jsonObject.toJSONString();
            }
        };
    }

    @Override
    public Map<String, RawDataType> getProducedRawData() {
        Map<String, RawDataType> map = new ConcurrentHashMap<>();
        map.put("positiveCounter", new RawDataType("number", DataType.INTEGER, true, CollectedType.INSTANT));
        map.put("negativeCounter", new RawDataType("number", DataType.INTEGER, true, CollectedType.INSTANT));
        map.put("mentions", new RawDataType("number", DataType.INTEGER, true, CollectedType.INSTANT));
        return map;
    }

    private void startCleanUpThread() {
        cleanUpService.submit(new Runnable() {
            @Override
            public void run() {
                while (true){
                    if(atomicBoolean.get()){
                        log.debug("delete from the list #tweets: " + listFoundItems.size());
                        listFoundItems.clear();
                        atomicBoolean.set(false);
                    }
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        log.error(e.getLocalizedMessage());
                    }
                }
            }
        });
    }

    @Override
    public String getName() {
        return NAME;
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
                if(SentimentAnalysis.isMatching(searchTerms, status.getText())){
                    log.debug("********* Found *************");
                    //log.debug("User is : " + status.getUser().getName());
                    //log.debug("Text is : " + status.getText());
                    listFoundItems.add(status.getText());
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
        // twitterStream.sample();
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
        twitterStream.shutdown();
        cleanUpService.shutdown();
    }

    public static void main(String[] args) throws TwitterException, IOException {
        final TwitterStreamSearchSensor twitterSentimentSensor = new TwitterStreamSearchSensor();
        twitterSentimentSensor.setProperty(SEARCH_TERMS, "nmbs vertraging gent antwerp");
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while(true){
                    try {
                        Thread.sleep(10000);
                        log.info("execute...");
                        SensorResult testResult = twitterSentimentSensor.execute(null);
                        log.info("RAW data: " + testResult.getRawData());
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
