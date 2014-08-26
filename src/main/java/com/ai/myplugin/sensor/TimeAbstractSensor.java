package com.ai.myplugin.sensor;

import com.ai.api.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
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
 * FIXME migrate to the cleaner Java8 date/time api
 */
public abstract class TimeAbstractSensor implements SensorPlugin {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    private final Gson gson = new Gson();

    String timeZone;
    private String dateString;
    protected static final String TIME_ZONE = "timezone";
    protected static final String DATE_FORMAT = "date";
    protected static final String DATE = "DATE";

    protected abstract String getSensorName();

    @Override
    public Map<String, PropertyType> getRequiredProperties() {
        Map<String, PropertyType> map = new HashMap<>();
        map.put(TIME_ZONE, new PropertyType(DataType.STRING, true, false));
        return map;
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
        if(TIME_ZONE.equalsIgnoreCase(s)) {
            return timeZone;
        }else if(DATE_FORMAT.equalsIgnoreCase(s)) {
            return dateString;
        }else {
            return null;
        }
    }

    @Override
    public SensorResult execute(SessionContext testSessionContext) {
        log.info("execute "+ getName() + ", sensor type:" + this.getClass().getName());

        String rawData = buildRawData();
        String state = getObserverState();
        return new TimeSensorResult(state, rawData);
    }

    private String buildRawData() {
        SimpleDateFormat date_format = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new GregorianCalendar().getTime();
        String dt = date_format.format(date);
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("date", dt);
        jsonObject.addProperty("UTC", date.getTime() / 1000);
        if (timeZone != null) {
            jsonObject.addProperty("timeZone", timeZone);
        }
        return gson.toJson(jsonObject);
    }

    protected abstract String getObserverState();

    protected final GregorianCalendar getNow() {
        return getProperty(TIME_ZONE) == null? new GregorianCalendar():
                    new GregorianCalendar(TimeZone.getTimeZone((String) getProperty(TIME_ZONE)));
    }

    @Override
    public String getName() {
        return getSensorName();
    }

    @Override
    public void setup(SessionContext testSessionContext) {
        log.debug("Setup : " + getName() + ", sensor : "+this.getClass().getName());
    }

    @Override
    public void shutdown(SessionContext testSessionContext) {
        log.debug("Shutdown : " + getName() + ", sensor : "+this.getClass().getName());
    }

    private static class TimeSensorResult implements SensorResult {
        private final String state;
        private final String rawData;

        public TimeSensorResult(final String state, final String rawData) {
            this.state = state;
            this.rawData = rawData;
        }

        @Override
        public boolean isSuccess() {
            return true;
        }

        @Override
        public String getObserverState() {
            return state;
        }

        @Override
        public List<Map<String, Number>> getObserverStates() {
            return Collections.emptyList();
        }

        @Override
        public String getRawData() {
            return rawData;
        }
    }
}
