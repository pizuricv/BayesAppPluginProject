package com.ai.myplugin.util;

import junit.framework.TestCase;

public class SequenceStatsTest extends TestCase {
    public void testSequenceStats()  {
        SequenceStats sequenceStats = new SequenceStats("OK,NOK,HELLO");
        sequenceStats.addSample("OK");
        assertFalse(sequenceStats.isMatching());
        sequenceStats.addSample("NOK");
        assertFalse(sequenceStats.isMatching());
        sequenceStats.addSample("HELLO");
        assertTrue(sequenceStats.isMatching());

        sequenceStats.addSample("HELLO");
        assertFalse(sequenceStats.isMatching());
        sequenceStats.addSample("HELLO");
        assertFalse(sequenceStats.isMatching());
        sequenceStats.addSample("HELLO");
        assertFalse(sequenceStats.isMatching());

    }

}