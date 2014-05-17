/**
 * Created by User: veselin
 * On Date: 19/03/14
 */
package com.ai.myplugin.sensor;

import com.ai.api.SensorResult;
import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class StockFormulaSensorTest extends TestCase{
    private static final Log log = LogFactory.getLog(StockFormulaSensorTest.class);

    public void testStockFormula(){
        StockFormulaSensor stockFormulaSensor = new StockFormulaSensor();
        stockFormulaSensor.setProperty("stock", "ALU");
        stockFormulaSensor.setProperty("threshold", 0);
        stockFormulaSensor.setProperty("formula", "<this.rawData.price> - <this.rawData.moving_average>");
        SensorResult SensorResult = stockFormulaSensor.execute(null);
        log.info(SensorResult.getObserverState());
        log.info(SensorResult.getRawData());


        stockFormulaSensor.setProperty("stock", "GOOG");
        stockFormulaSensor.setProperty("threshold", .15);
        stockFormulaSensor.setProperty("formula", "(<this.rawData.price> - <this.rawData.moving_average>)/<this.rawData.moving_average>");
        SensorResult = stockFormulaSensor.execute(null);
        log.info(SensorResult.getObserverState());
        log.info(SensorResult.getRawData());
    }
}
