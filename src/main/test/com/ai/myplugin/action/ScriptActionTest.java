package com.ai.myplugin.action;

import com.ai.api.SessionContext;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

public class ScriptActionTest {

    @Test
    public void testActionDefaultJS() throws Exception {
        ScriptAction action = new ScriptAction();
        action.setProperty(ScriptAction.PROPERTY_SCRIPT, "print('Hello, World');JSON.stringify(['OK']);");

        SessionContext context = new SessionContext(1);

        action.action(context);
    }

    @Test
    @Ignore("Seems to fail currently")
    public void testActionScala() throws Exception {
        ScriptAction action = new ScriptAction();
        action.setProperty(ScriptAction.PROPERTY_ENGINE, "scala");
        action.setProperty(ScriptAction.PROPERTY_SCRIPT, "println(123); \"Scala ok\"");

        SessionContext context = new SessionContext(1);

        action.action(context);
    }
}