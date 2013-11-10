package com.ai.myplugin.sensor;

import com.ai.bayes.plugins.BNSensorPlugin;
import com.ai.bayes.scenario.TestResult;
import com.ai.myplugin.action.MailAction;
import com.ai.myplugin.util.EmptyTestResult;
import com.ai.myplugin.util.SlidingWindowCounter;
import com.ai.myplugin.util.TwitterConfig;
import com.ai.myplugin.util.Utils;
import com.ai.util.resource.NodeSessionParams;
import com.ai.util.resource.TestSessionContext;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import twitter4j.*;
import twitter4j.TwitterException;
import java.io.IOException;
import java.util.*;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by User: veselin
 * On Date: 25/10/13
 */
@PluginImplementation
public class TwitterQuerySensor implements BNSensorPlugin{
    private static final Log log = LogFactory.getLog(TwitterQuerySensor.class);
    private static final String SEARCH_TERMS = "search_terms";
    private static final String TIME_ZONE = "time_zone";
    private static final String FROM = "from";
    private static final String DATE = "date";
    private Map<String, Object> propertiesMap = new ConcurrentHashMap<String, Object>();
    Twitter twitter = new TwitterFactory(TwitterConfig.getTwitterConfigurationBuilder()).getInstance();




    @Override
    public String[] getRequiredProperties() {
        return new String []{SEARCH_TERMS, DATE, FROM, TIME_ZONE};
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
        return "TwitterQuerySensor";
    }

    @Override
    public TestResult execute(TestSessionContext testSessionContext) {
        log.info("execute " + getName() + ", sensor type:" + this.getClass().getName());
        if(getProperty(SEARCH_TERMS) == null && getProperty(FROM) == null){
            return new EmptyTestResult();
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
        QueryResult result = null;
        final ArrayList<String> listTweets = new ArrayList<String>();

        try {
            result = twitter.search(query);
        } catch (TwitterException e) {
            log.error(e.getLocalizedMessage());
            e.printStackTrace();
        }
        for (Status status : result.getTweets()) {
            log.debug("@" + status.getUser().getScreenName() + ":" + status.getText());
            listTweets.add(status.getText());
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
        return "TwitterQuerySensor";
    }

    @Override
    public String[] getSupportedStates() {
        return new String[] {"Found", "Not Found"};
    }

    @Override
    public void shutdown(TestSessionContext testSessionContext) {
        log.debug("Shutdown : " + getName() + ", sensor : "+this.getClass().getName());

    }

    public static void main(String[] args) throws TwitterException, IOException {
        TwitterQuerySensor twitterQuerySensor = new TwitterQuerySensor();
        twitterQuerySensor.setProperty(FROM, "nmbs");
        twitterQuerySensor.setProperty(DATE, "2013-10-12");
        TestSessionContext testSessionContext = new TestSessionContext(1);
        TestResult result = twitterQuerySensor.execute(testSessionContext);
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
        testSessionContext.setAttribute(NodeSessionParams.RAW_DATA, mapTestResult);
        mail.action(testSessionContext);
    }

}
