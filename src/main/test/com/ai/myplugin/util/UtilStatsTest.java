/**
 * Created by User: veselin
 * On Date: 26/03/14
 */

package com.ai.myplugin.util;

import junit.framework.TestCase;

public class UtilStatsTest extends TestCase{

    public void testAddSample() throws Exception {
        UtilStats utilStats = new UtilStats();
        utilStats.addSample(1);
        utilStats.addSample(2);
        utilStats.addSample(3);
        utilStats.addSample(4);
        utilStats.addSample(5);
        utilStats.addSample(6);
        assertEquals(21.0/6, utilStats.avg);
        assertEquals(1.0, utilStats.min);
        assertEquals(6.0, utilStats.max);
        System.out.println(utilStats);

    }

    public void testStatsWithLength() throws Exception {
        UtilStats utilStats = new UtilStats(3);
        utilStats.addSample(1);
        utilStats.addSample(2);
        utilStats.addSample(3);
        utilStats.addSample(4);
        utilStats.addSample(5);
        utilStats.addSample(6);
        assertEquals(5.0, utilStats.avg);
        assertEquals(4.0, utilStats.min);
        assertEquals(6.0, utilStats.max);
        System.out.println(utilStats);
    }

    public void testStatsWithLength2() throws Exception {
        UtilStats utilStats = new UtilStats(3);
        utilStats.addSample(1);
        utilStats.addSample(2);
        utilStats.addSample(3);
        utilStats.addSample(4);
        utilStats.addSample(5);
        assertEquals(4.0, utilStats.avg);
        assertEquals(3.0, utilStats.min);
        assertEquals(5.0, utilStats.max);
        System.out.println(utilStats);
    }

    public void testStatsWithLength3() throws Exception {
        UtilStats utilStats = new UtilStats(3);
        utilStats.addSample(1);
        utilStats.addSample(2);
        assertEquals(1.5, utilStats.avg);
        assertEquals(1.0, utilStats.min);
        assertEquals(2.0, utilStats.max);
        System.out.println(utilStats);
    }

    public void testStatsWithLength4() throws Exception {
        UtilStats utilStats = new UtilStats(3);
        utilStats.addSample(1);
        assertEquals(1.0, utilStats.avg);
        assertEquals(1.0, utilStats.min);
        assertEquals(1.0, utilStats.max);
        assertEquals(0.0, utilStats.stdev);
        System.out.println(utilStats);
    }
}
