package com.ai.myplugin.sensor;

import com.ai.bayes.scenario.TestResult;
import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Created by User: veselin
 * On Date: 19/03/14
 */
public class StockFormulaSensorTest extends TestCase{
    private static final Log log = LogFactory.getLog(StockFormulaSensorTest.class);

    public void testStockFormula(){
        StockFormulaSensor stockFormulaSensor = new StockFormulaSensor();
        stockFormulaSensor.setProperty("stock", "ALU");
        stockFormulaSensor.setProperty("threshold", 0);
        stockFormulaSensor.setProperty("formula", "<this.rawData.price> - <this.rawData.moving_average>");
        TestResult testResult = stockFormulaSensor.execute(null);
        log.info(testResult.getObserverState());
        log.info(testResult.getRawData());


        stockFormulaSensor.setProperty("stock", "GOOG");
        stockFormulaSensor.setProperty("threshold", .15);
        stockFormulaSensor.setProperty("formula", "(<this.rawData.price> - <this.rawData.moving_average>)/<this.rawData.moving_average>");
        testResult = stockFormulaSensor.execute(null);
        log.info(testResult.getObserverState());
        log.info(testResult.getRawData());
    }
}
