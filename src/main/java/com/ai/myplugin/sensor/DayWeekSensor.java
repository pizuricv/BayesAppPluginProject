package com.ai.myplugin.sensor;

import net.xeoh.plugins.base.annotations.PluginImplementation;


@PluginImplementation
public class DayWeekSensor extends TimeAbstractSensor {
    @Override
    protected String getTag() {
        return TimeAbstractSensor.DAY_WEEK;
    }

    @Override
    protected String getSensorName() {
        return "DayInWeek";
    }

    public static void main (String []args){
        DayWeekSensor daySensor = new DayWeekSensor();
        log.debug(daySensor.execute(null).getObserverState());
    }
}
