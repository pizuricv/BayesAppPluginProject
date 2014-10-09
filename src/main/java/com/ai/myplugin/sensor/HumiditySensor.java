
/**
 * User: pizuricv
 */
package com.ai.myplugin.sensor;

import com.ai.api.PluginHeader;
import com.ai.api.SensorResult;
import net.xeoh.plugins.base.annotations.PluginImplementation;

@PluginImplementation
@PluginHeader(version = "1.0.1", author = "Veselin", category = "Weather", iconURL = "http://app.waylay.io/icons/humidity.png")
public class HumiditySensor extends WeatherAbstractSensor{

    @Override
    protected String getTag() {
        return WeatherAbstractSensor.HUMIDITY;
    }

    @Override
    protected String getSensorName() {
        return "Humidity";
    }

    @Override
    public String getDescription() {
        return "Humidity sensor";
    }

}
