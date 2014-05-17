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

        assertTrue("{2=1, 3=1, 4=3}".equals(map1.toString()));
        assertTrue("{2=1, 3=2, 4=4, 5=1}".equals(map2.toString()));
        assertTrue("{2=0, 3=2, 4=2, 5=2}".equals(map3.toString()));
    }

    public void testIncrementCountText() throws Exception {
        SlidingWindowCounter<String> slidingWindowCounter = new SlidingWindowCounter<String>(2);
        slidingWindowCounter.incrementCount("hello");
        slidingWindowCounter.incrementCount("hello2");
        slidingWindowCounter.incrementCount("hello");
        slidingWindowCounter.incrementCount("hello");
        slidingWindowCounter.incrementCount("hello");
        Map map1 = slidingWindowCounter.getCountsThenAdvanceWindow();

        assertTrue("{hello=4, hello2=1}".equals(map1.toString()));

    }

}
