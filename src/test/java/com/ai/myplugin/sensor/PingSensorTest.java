/**
 * Created by User: veselin
 * On Date: 27/02/14
 */
package com.ai.myplugin.sensor;

import com.ai.api.DataType;
import com.ai.api.RawDataType;
import com.ai.api.SensorResult;
import com.ai.api.SessionContext;
import com.ai.myplugin.TestSessionContext;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;


public class PingSensorTest{

    @Test
    public void testExecute() throws Exception {
        PingSensor pingSensor = new PingSensor();
        pingSensor.setProperty("address", "www.waylay.io");
        SensorResult testResult = pingSensor.execute(new TestSessionContext());
        System.out.println(testResult.getRawData());
        assertEquals("Alive", testResult.getObserverState());
    }

    @Test
    public void testExecuteUppercaseProperty() throws Exception {
        PingSensor pingSensor = new PingSensor();
        pingSensor.setProperty("ADDRESS", "www.waylay.io");
        SensorResult testResult = pingSensor.execute(new TestSessionContext());
        System.out.println(testResult.getRawData());
        assertEquals("Alive", testResult.getObserverState());
    }

    @Test
    public void testRawDataMetadata() throws Exception {
        PingSensor pingSensor = new PingSensor();
        pingSensor.setProperty("ADDRESS", "www.waylay.io");
        SensorResult testResult = pingSensor.execute(new TestSessionContext());
        Map<String, RawDataType> map = pingSensor.getProducedRawData();
        JSONObject obj = (JSONObject) new JSONParser().parse(testResult.getRawData());
        assertEquals(map.get("time").getDataType(), DataType.DOUBLE);
        assertEquals(map.get("result").getDataType(), DataType.STRING);
        try{
            Double.parseDouble(obj.get("time").toString());
        } catch (Exception e){
            fail("result should be double");
        }

        try{
            Double.parseDouble(obj.get("result").toString());
            fail("result should not be double");
        } catch (Exception e){

        }
    }


    @Test
    public void testDown() throws Exception {
        PingSensor pingSensor = new PingSensor();
        pingSensor.setProperty("address", "www.waylaay.io");
        SensorResult testResult = pingSensor.execute(new TestSessionContext());
        System.out.println(testResult.getRawData());
        assertEquals("Not Alive", testResult.getObserverState());
    }
}
