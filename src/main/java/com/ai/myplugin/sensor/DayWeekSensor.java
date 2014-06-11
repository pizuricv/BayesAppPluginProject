package com.ai.myplugin.sensor;

import com.ai.api.PluginHeader;
import net.xeoh.plugins.base.annotations.PluginImplementation;


@PluginImplementation
@PluginHeader(version = "1.0.1", author = "Veselin", category = "Time", iconURL = "http://app.waylay.io/icons/week.png")
public class DayWeekSensor extends TimeAbstractSensor {
    @Override
    protected String getTag() {
        return TimeAbstractSensor.DAY_WEEK;
    }

    @Override
    protected String getSensorName() {
        return "DayInWeek";
    }

    @Override
    public String getDescription() {
        return "Returns day in a week";
    }

}
