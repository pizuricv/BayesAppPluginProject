package com.ai.myplugin.sensor;

import com.ai.api.PluginHeader;
import net.xeoh.plugins.base.annotations.PluginImplementation;

import java.text.DateFormatSymbols;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;


@PluginImplementation
@PluginHeader(version = "1.0.1", author = "Veselin", category = "Time", iconURL = "http://app.waylay.io/icons/week.png")
public class DayWeekSensor extends TimeAbstractSensor {

    @Override
    protected String getSensorName() {
        return "DayInWeek";
    }

    @Override
    public String getDescription() {
        return "Returns day in a week";
    }

    @Override
    protected String getObserverState() {
        return new DateFormatSymbols().getWeekdays()[getNow().get(Calendar.DAY_OF_WEEK)];
    }

    @Override
    public Set<String> getSupportedStates() {
        return new HashSet<>(Arrays.asList(new String[]{"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"}));
    }
}
