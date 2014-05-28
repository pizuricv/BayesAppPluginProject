package com.ai.myplugin.action;

import org.junit.Test;

import static org.junit.Assert.*;

public class NodeJSActionTest {

    @Test
    public void testAction() throws Exception {
        NodeJSAction nodeJSAction = new NodeJSAction();
        nodeJSAction.getRequiredProperties();
        String javaScript =
                "a = { observedState:\"world\"};\n" +
                "console.log(a)" ;
        nodeJSAction.setProperty("javaScript", javaScript);
        nodeJSAction.action(null);
        // hard to assert the println but at least it does not fail
    }
}