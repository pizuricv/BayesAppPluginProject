package com.ai.myplugin.sensor;

import com.ai.api.PluginHeader;
import net.xeoh.plugins.base.annotations.PluginImplementation;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;


@PluginImplementation
@PluginHeader(version = "1.0.1", author = "Veselin", category = "Time", iconURL = "http://app.waylay.io/icons/day.png")
public class DaySensor extends TimeAbstractSensor {

    @Override
    protected String getSensorName() {
        return "Day";
    }

    @Override
    public String getDescription() {
        return "Returns day in a month";
    }

    @Override
    public Set<String> getSupportedStates() {
        return new HashSet<>(Arrays.asList(new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17",
                "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31"}));
    }

    @Override
    protected String getObserverState() {
        return String.valueOf(getNow().get(Calendar.DAY_OF_MONTH));
    }
}
