package com.ai.myplugin.action;

import com.ai.api.SessionContext;
import com.ai.api.SessionParams;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

public class NetworkWireTest {

    @Test
    @Ignore("We should start a dummy service to test this locally")
    public void testAction() throws Exception {
        NetworkWire networkWire = new NetworkWire();
        networkWire.setProperty(NetworkWire.SERVER_ADDRESS, "http://107.170.20.30/api");
        networkWire.setProperty(NetworkWire.SCENARIO_ID, "1");
        SessionContext testSessionContext =  new SessionContext(1);
        testSessionContext.setAttribute(SessionParams.NODE_NAME, "CONNECTION");
        testSessionContext.setAttribute(SessionParams.NODE_TRIGGERED_STATE, "NOK");
        networkWire.action(testSessionContext);
    }
}