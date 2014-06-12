/**
 * Created by User: veselin
 * On Date: 25/10/13
 */

package com.ai.myplugin.sensor;


import com.ai.api.*;
import com.ai.myplugin.action.MailAction;
import com.ai.myplugin.util.SensorResultBuilder;
import com.ai.myplugin.util.TwitterConfig;
import net.xeoh.plugins.base.annotations.PluginImplementation;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.*;
import twitter4j.TwitterException;
import java.io.IOException;
import java.util.*;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;


@PluginImplementation
@PluginHeader(version = "1.0.1", author = "Veselin", category = "Social", iconURL = "http://app.waylay.io/icons/twitter_query.png")
public class TwitterQuerySensor implements SensorPlugin {

    private static final Logger log = LoggerFactory.getLogger(TwitterQuerySensor.class);

    private static final String SEARCH_TERMS = "search_terms";
    private static final String TIME_ZONE = "time_zone";
    private static final String FROM = "from";
    private static final String DATE = "date";
    private static final String NAME = "TwitterQuery";
    private Map<String, Object> propertiesMap = new ConcurrentHashMap<String, Object>();
    Twitter twitter = new TwitterFactory(TwitterConfig.getTwitterConfigurationBuilder()).getInstance();
    private String[] states = {"Found", "Not Found"};

    @Override
    public Map<String, PropertyType> getRequiredProperties() {
        Map<String, PropertyType> map = new HashMap<>();
        map.put(SEARCH_TERMS, new PropertyType(DataType.STRING, true, false));
        map.put(DATE, new PropertyType(DataType.STRING, true, false));
        map.put(FROM, new PropertyType(DataType.STRING, true, false));
        map.put(TIME_ZONE, new PropertyType(DataType.STRING, true, false));
        return map;
    }

    @Override
    public Map<String,PropertyType> getRuntimeProperties() {
        return new HashMap<>();
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
        return "Twitter Query Sensor, search for the keywords and returns array of tweets that match the query";
    }

    @Override
    public SensorResult execute(SessionContext testSessionContext) {
        log.info("execute " + getName() + ", sensor type:" + this.getClass().getName());
        if(getProperty(SEARCH_TERMS) == null && getProperty(FROM) == null){
            return SensorResultBuilder.failure().build();
        }
        String from = getProperty(FROM) == null? "" : "from:"+getProperty(FROM);
        String searchTerm = getProperty(SEARCH_TERMS) == null? "" : " "+ getProperty(SEARCH_TERMS);
        String date = (String) getProperty(DATE);
        if(date == null){
            GregorianCalendar gregorianCalendar =  getProperty(TIME_ZONE) == null? new GregorianCalendar():
                    new GregorianCalendar(TimeZone.getTimeZone((String) getProperty(TIME_ZONE)));
            date = Integer.toString(gregorianCalendar.get(Calendar.YEAR)) + "-" +(gregorianCalendar.get(Calendar.MONTH) +1 )
                    + "-" +gregorianCalendar.get(Calendar.DAY_OF_MONTH);

        }
        String queryString = from + searchTerm;
        log.info("queryString: "+ queryString + " , from date: "+date);

        Query query = new Query(queryString);
        query.setSince(date);
        QueryResult result;
        final ArrayList<String> listTweets = new ArrayList<String>();

        try {
            result = twitter.search(query);
        } catch (TwitterException e) {
            log.error(e.getLocalizedMessage(), e);
            return SensorResultBuilder.failure().build();
        }
        for (Status status : result.getTweets()) {
            log.debug("@" + status.getUser().getScreenName() + ":" + status.getText());
            listTweets.add(status.getText());
        }

        return new SensorResult() {
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
                if(listTweets.size() == 0)
                    return "Not Found";
                return "Found";
            }

            @Override
            public List<Map<String, Number>> getObserverStates() {
                return null;
            }

            @Override
            public String getRawData() {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("tweets", listTweets);
                return jsonObject.toJSONString();
            }
        };
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Set<String> getSupportedStates() {
        return new HashSet(Arrays.asList(states));
    }

    @Override
    public void setup(SessionContext testSessionContext) {
        log.debug("Setup : " + getName() + ", sensor : "+this.getClass().getName());
    }

    @Override
    public void shutdown(SessionContext testSessionContext) {
        log.debug("Shutdown : " + getName() + ", sensor : "+this.getClass().getName());

    }

    public static void main(String[] args) throws TwitterException, IOException {
        TwitterQuerySensor twitterQuerySensor = new TwitterQuerySensor();
        twitterQuerySensor.setProperty(FROM, "nmbs");
        twitterQuerySensor.setProperty(DATE, "2013-10-12");
        SessionContext testSessionContext = new SessionContext(1);
        SensorResult result = twitterQuerySensor.execute(testSessionContext);
        log.info(result.getObserverState());
        log.info(result.getRawData());


        MailAction mail = new MailAction();
        mail.setProperty("address", "veselin.pizurica@gmail.com");
        mail.setProperty("subject", "test the action");
        mail.setProperty("message", "hello vele node1->tweets");

        Map<String, Object> mapTestResult = new HashMap<String, Object>();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("rawData", result.getRawData());
        mapTestResult.put("node1", jsonObject);
        testSessionContext.setAttribute(SessionParams.RAW_DATA, mapTestResult);
        mail.action(testSessionContext);
    }

}
