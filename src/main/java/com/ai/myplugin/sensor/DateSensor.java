package com.ai.myplugin.sensor;

import com.ai.api.DataType;
import com.ai.api.PluginHeader;
import com.ai.api.PropertyType;
import net.xeoh.plugins.base.annotations.PluginImplementation;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;


@PluginImplementation
@PluginHeader(version = "1.0.1", author = "Veselin", category = "Time", iconURL = "http://app.waylay.io/icons/calendar.png")
public class DateSensor extends TimeAbstractSensor {

    public static final String STATE_TRUE = "true";
    public static final String STATE_FALSE = "false";

    @Override
    protected String getSensorName() {
        return "Date";
    }

    @Override
    public String getDescription() {
        return "Returns date";
    }

    @Override
    public Map<String, PropertyType> getRequiredProperties() {
        Map<String, PropertyType> map = new HashMap<>();
        map.put(TIME_ZONE, new PropertyType(DataType.STRING, true, false));
        map.put(DATE, new PropertyType(DataType.DATE, true, false));
        return map;
    }

    @Override
    protected String getObserverState() {
        try {
            Instant fromIso8601 = Instant.parse((String) getProperty(DATE_FORMAT));
            ZoneId zone = ZoneId.of((String) getProperty(TIME_ZONE));
            LocalDate local = fromIso8601.atZone(zone).toLocalDate();

            LocalDate now = LocalDate.now(zone);

            return local.equals(now) ? STATE_TRUE : STATE_FALSE;
        } catch (DateTimeException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Set<String> getSupportedStates() {
        return new HashSet<>(Arrays.asList(new String []{ STATE_TRUE, STATE_FALSE}));
    }
}
