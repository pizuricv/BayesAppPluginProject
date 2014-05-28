package com.ai.myplugin.util;

import org.junit.Test;
import static org.junit.Assert.*;

public class UtilsTest {

    @Test
    public void testGetDouble() {
        assertEquals(23.0, Utils.getDouble(new Integer(23)), 0.0);
        assertEquals(23.0, Utils.getDouble(new Double(23)), 0.0);
        assertEquals(23.0, Utils.getDouble("23"), 0.0);
        assertEquals(23.0, Utils.getDouble(23), 0.0);
    }
}