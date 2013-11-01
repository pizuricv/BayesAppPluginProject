package com.ai.myplugin.sensor;

import net.xeoh.plugins.base.annotations.PluginImplementation;


@PluginImplementation
public class DaySensor extends TimeAbstractSensor {
    @Override
    protected String getTag() {
        return TimeAbstractSensor.DAY;
    }

    @Override
    protected String getSensorName() {
        return "Day";
    }

    public static void main (String []args){
        DaySensor daySensor = new DaySensor();
        log.debug(daySensor.execute(null).getObserverState());
    }
}
