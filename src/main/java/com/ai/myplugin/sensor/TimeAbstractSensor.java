/**
 * User: pizuricv
 * Date: 5/17/13
 */

package com.ai.myplugin.sensor;

import com.ai.api.*;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

/*
 * FIXME ugly design
 * FIXME migrate to the cleaner Java8 date/time api
 */
public abstract class TimeAbstractSensor implements SensorPlugin {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    String timeZone;
    private String dateString;
    String [] returnStates;
    protected static final String TIME_ZONE = "timezone";
    protected static final String DATE_FORMAT = "date";
    protected static final String HOUR = "HOUR";
    protected static final String DAY = "DAY";
    protected static final String DAY_WEEK = "DAY_WEEK";
    protected static final String DATE = "DATE";
    protected static final String WEEK = "WEEK";
    protected static final String MONTH = "MONTH";


    protected abstract String getTag();
    protected abstract String getSensorName();


    @Override
    public Map<String, PropertyType> getRequiredProperties() {
        Map<String, PropertyType> map = new HashMap<>();
        map.put(TIME_ZONE, new PropertyType(DataType.STRING, true, false));
        return map;
    }

    @Override
    public Map<String,PropertyType> getRuntimeProperties() {
        return new HashMap<>();
    }

    //@TODO implement the time zone
    @Override
    public void setProperty(String s, Object o) {
        if(TIME_ZONE.equalsIgnoreCase(s))
            timeZone = (String) o;
        else if(DATE_FORMAT.equalsIgnoreCase(s))
            dateString = (String) o;
        else {
           throw new RuntimeException("Property "+ s + " not in the required settings");
        }
    }

    @Override
    public Object getProperty(String s) {
        if(TIME_ZONE.equalsIgnoreCase(s))
            return timeZone;
        else if(DATE_FORMAT.equalsIgnoreCase(s))
            return dateString;
        else
            return null;
    }

    @Override
    public String getDescription() {
        return "Returns hour, day(in a week) or month";
    }

    @Override
    public SensorResult execute(SessionContext testSessionContext) {
        log.info("execute "+ getName() + ", sensor type:" +this.getClass().getName());
        return new SensorResult() {
            @Override
            public boolean isSuccess() {
                return true;
            }

            @Override
            public String getName() {
                return "Date result";
            }

            @Override
            public String getObserverState() {
                GregorianCalendar gregorianCalendar = getProperty(TIME_ZONE) == null? new GregorianCalendar():
                        new GregorianCalendar(TimeZone.getTimeZone((String) getProperty(TIME_ZONE)));
                if(HOUR.equalsIgnoreCase(getTag())) {
                    return String.valueOf(gregorianCalendar.get(Calendar.HOUR_OF_DAY));
                } else if(DAY_WEEK.equalsIgnoreCase(getTag())) {
                    return new DateFormatSymbols().getWeekdays()[gregorianCalendar.get(Calendar.DAY_OF_WEEK)];
                } else if(DAY.equalsIgnoreCase(getTag())) {
                    return String.valueOf(gregorianCalendar.get(Calendar.DAY_OF_MONTH));
                } else if(WEEK.equalsIgnoreCase(getTag())) {
                    return String.valueOf(gregorianCalendar.get(Calendar.WEEK_OF_MONTH));
                } else if(MONTH.equalsIgnoreCase(getTag())) {
                    return new DateFormatSymbols().getMonths()[gregorianCalendar.get(Calendar.MONTH)];
                } else if(DATE.equalsIgnoreCase(getTag())) {
                    try {
                        Instant fromIso8601 = Instant.parse((String) getProperty(DATE_FORMAT));
                        ZoneId zone = ZoneId.of((String) getProperty(TIME_ZONE));
                        LocalDate local = fromIso8601.atZone(zone).toLocalDate();

                        LocalDate now = LocalDate.now(zone);


                        return local.equals(now) ?  "true" : "false";
                    } catch (DateTimeException e) {
                        log.error(e.getLocalizedMessage(), e);
                        throw new RuntimeException(e);
                    }
                }
                return "";
            }

            @Override
            public List<Map<String, Number>> getObserverStates() {
                return null;
            }

            @Override
            public String getRawData() {
                SimpleDateFormat date_format = new SimpleDateFormat("yyyy-MM-dd");
                Date date = new GregorianCalendar().getTime();
                String dt = date_format.format(date);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("date", dt);
                jsonObject.put("UTC", date.getTime() / 1000);
                if (timeZone != null)
                    jsonObject.put("timeZone", timeZone);
                return jsonObject.toString();
            }
        };
    }

    @Override
    public String getName() {
        return getSensorName();
    }

    @Override
    public Set<String> getSupportedStates() {
        if(getTag().equalsIgnoreCase(HOUR)) {
            returnStates = new String []{"1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17",
                    "18","19","20","21","22","23","24"};
        } else if(getTag().equalsIgnoreCase(DAY_WEEK)) {
            returnStates = new String []{"Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday"};
        } else if(getTag().equalsIgnoreCase(DAY)) {
            returnStates = new String []{"1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17",
                    "18","19","20","21","22","23","24", "25", "26", "27", "28", "29", "30", "31"};
        } else if(getTag().equalsIgnoreCase(WEEK)) {
            returnStates = new String []{"1","2","3","4","5"};
        } else if(getTag().equalsIgnoreCase(MONTH)) {
            returnStates = new String []{"January","February","March","April","May","June","July",
                    "August","September","October","November","December"};
        } else if(getTag().equalsIgnoreCase(DATE)) {
            returnStates = new String []{"true", "false"};
        }
        return new HashSet(Arrays.asList(returnStates));
    }

    @Override
    public void setup(SessionContext testSessionContext) {
        log.debug("Setup : " + getName() + ", sensor : "+this.getClass().getName());
    }

    @Override
    public void shutdown(SessionContext testSessionContext) {
        log.debug("Shutdown : " + getName() + ", sensor : "+this.getClass().getName());
    }
}
