package com.ai.myplugin.sensor;

import net.xeoh.plugins.base.annotations.PluginImplementation;


@PluginImplementation
public class DateSensor extends TimeAbstractSensor {
    @Override
    protected String getTag() {
        return TimeAbstractSensor.DATE;
    }

    @Override
    protected String getSensorName() {
        return "Date";
    }

    @Override
    public String[] getRequiredProperties() {
        return new String[] {TIME_ZONE, DATE};
    }

    public static void main (String []args){
        DateSensor daySensor = new DateSensor();
        daySensor.setProperty(DATE, "2013-09-06");
        daySensor.setProperty(TIME_ZONE, "UTC");
        System.out.println(daySensor.execute(null).getObserverState());
    }
}
