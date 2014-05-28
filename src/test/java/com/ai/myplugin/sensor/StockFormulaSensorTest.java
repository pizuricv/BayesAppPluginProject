/**
 * Created by User: veselin
 * On Date: 19/03/14
 */
package com.ai.myplugin.sensor;

import com.ai.api.SensorResult;
import junit.framework.TestCase;

public class StockFormulaSensorTest extends TestCase{

    public void testStockFormula(){
        StockFormulaSensor stockFormulaSensor = new StockFormulaSensor();
        stockFormulaSensor.setProperty("stock", "ALU");
        stockFormulaSensor.setProperty("threshold", 0);
        stockFormulaSensor.setProperty("formula", "<this.rawData.price> - <this.rawData.moving_average>");
        SensorResult SensorResult = stockFormulaSensor.execute(null);
        System.out.println(SensorResult.getObserverState());
        System.out.println(SensorResult.getRawData());


        stockFormulaSensor.setProperty("stock", "GOOG");
        stockFormulaSensor.setProperty("threshold", .15);
        stockFormulaSensor.setProperty("formula", "(<this.rawData.price> - <this.rawData.moving_average>)/<this.rawData.moving_average>");
        SensorResult = stockFormulaSensor.execute(null);
        System.out.println(SensorResult.getObserverState());
        System.out.println(SensorResult.getRawData());
    }
}
