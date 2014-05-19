/**
 * Created by User: veselin
 * On Date: 24/03/14
 */

package com.ai.myplugin.util;

import junit.framework.TestCase;
import java.util.Map;

public class SlidingWindowCounterTest extends TestCase{

    public void testIncrementCount() throws Exception {
        SlidingWindowCounter<Long> slidingWindowCounter = new SlidingWindowCounter<Long>(2);
        slidingWindowCounter.incrementCount(4l);
        slidingWindowCounter.incrementCount(4l);
        slidingWindowCounter.incrementCount(2l);
        slidingWindowCounter.incrementCount(3l);
        slidingWindowCounter.incrementCount(4l);
        Map map1 = slidingWindowCounter.getCountsThenAdvanceWindow();
        slidingWindowCounter.incrementCount(5l);
        slidingWindowCounter.incrementCount(3l);
        slidingWindowCounter.incrementCount(4l);
        Map map2 = slidingWindowCounter.getCountsThenAdvanceWindow();

        slidingWindowCounter.incrementCount(5l);
        slidingWindowCounter.incrementCount(3l);
        slidingWindowCounter.incrementCount(4l);
        Map map3 = slidingWindowCounter.getCountsThenAdvanceWindow();

        assertEquals("{2=1, 3=1, 4=3}", map1.toString());
        assertEquals("{2=1, 3=2, 4=4, 5=1}", map2.toString());
        assertEquals("{2=0, 3=2, 4=2, 5=2}", map3.toString());
    }

    public void testIncrementCountText() throws Exception {
        SlidingWindowCounter<String> slidingWindowCounter = new SlidingWindowCounter<String>(2);
        slidingWindowCounter.incrementCount("hello");
        slidingWindowCounter.incrementCount("hello2");
        slidingWindowCounter.incrementCount("hello");
        slidingWindowCounter.incrementCount("hello");
        slidingWindowCounter.incrementCount("hello");
        Map<String, Long> map1 = slidingWindowCounter.getCountsThenAdvanceWindow();

        assertEquals(4, map1.get("hello").longValue());
        assertEquals(1, map1.get("hello2").longValue());
    }

}
