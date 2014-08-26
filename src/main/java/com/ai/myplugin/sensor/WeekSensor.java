package com.ai.myplugin.sensor;

import com.ai.api.PluginHeader;
import net.xeoh.plugins.base.annotations.PluginImplementation;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;


@PluginImplementation
@PluginHeader(version = "1.0.1", author = "Veselin", category = "Time", iconURL = "http://app.waylay.io/icons/week.png")
public class WeekSensor extends TimeAbstractSensor {

    @Override
    protected String getSensorName() {
        return "Week";
    }

    @Override
    public String getDescription() {
        return "Returns week in a month";
    }

    @Override
    public Set<String> getSupportedStates() {
        return new HashSet<>(Arrays.asList(new String []{"1","2","3","4","5"}));
    }

    @Override
    protected String getObserverState() {
        return String.valueOf(getNow().get(Calendar.WEEK_OF_MONTH));
    }
}
