package com.ai.myplugin.action;

import com.ai.api.SessionContext;
import com.ai.myplugin.TestSessionContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class HueActionTest {

    private HueAction action;

    @Before
    public void startAction(){
        this.action = new HueAction();
    }

    @After
    public void stopAction(){
        this.action.shutDown();
    }

    @Test
    @Ignore("Integration test for when hue bridge available")
    public void testAction() throws Exception {
        SessionContext context = new TestSessionContext();

        action.waitForConnection(2, TimeUnit.MINUTES);

        action.action(context);
    }
}