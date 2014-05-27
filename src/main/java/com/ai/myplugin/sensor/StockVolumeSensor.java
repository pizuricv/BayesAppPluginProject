/**
 * User: pizuricv
 * Date: 6/4/13
 */

package com.ai.myplugin.sensor;

import com.ai.api.PluginHeader;
import net.xeoh.plugins.base.annotations.PluginImplementation;

import java.util.*;

@PluginImplementation
@PluginHeader(version = "1.0.1", author = "Veselin", category = "Stock", iconURL = "http://app.waylay.io/icons/mvolume.png")
public class StockVolumeSensor extends StockAbstractSensor {

    @Override
    protected String getTag() {
        return StockAbstractSensor.VOLUME;
    }

    @Override
    protected String getSensorName() {
        return "StockVolume";
    }

    public static void main(String[] args){
        StockVolumeSensor stockSensor = new StockVolumeSensor();
        stockSensor.setProperty(STOCK, "MSFT");
        stockSensor.setProperty(THRESHOLD, "36");
        log.debug(Arrays.toString(stockSensor.getSupportedStates()));
        log.debug(stockSensor.execute(null).getObserverState());

        stockSensor.setProperty(STOCK, "GOOG");
        stockSensor.setProperty(THRESHOLD, "800.0");
        log.debug(stockSensor.execute(null).getObserverState());

        stockSensor.setProperty(STOCK, "BAR.BR");
        stockSensor.setProperty(THRESHOLD, "-1.0");
        log.debug(stockSensor.execute(null).getObserverState());
    }
}
