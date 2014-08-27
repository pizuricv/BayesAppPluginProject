package com.ai.myplugin.sensor;

import com.ai.api.PluginHeader;
import net.xeoh.plugins.base.annotations.PluginImplementation;

import java.text.DateFormatSymbols;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;


@PluginImplementation
@PluginHeader(version = "1.0.1", author = "Veselin", category = "Time", iconURL = "http://app.waylay.io/icons/month.png")
public class MonthSensor extends TimeAbstractSensor {

    @Override
    protected String getSensorName() {
        return "Month";
    }

    @Override
    public String getDescription() {
        return "Returns month in a year";
    }

    @Override
    protected String getObserverState() {
        // TODO this might not work on a different locale
        return new DateFormatSymbols().getMonths()[getNow().get(Calendar.MONTH)];
    }

    @Override
    public Set<String> getSupportedStates() {
        return new HashSet<>(Arrays.asList(new String []{"January","February","March","April","May","June","July",
                "August","September","October","November","December"}));
    }
}