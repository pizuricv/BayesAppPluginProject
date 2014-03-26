package com.ai.myplugin.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by User: veselin
 * On Date: 26/03/14
 */
public class SlidingWindowStatsCounter {
    private static final Log log = LogFactory.getLog(SlidingWindowStatsCounter.class);
    private final String name;
    private int slidingWindowMinutes = 5;
    ConcurrentHashMap<Long, UtilStats> map = new ConcurrentHashMap<Long, UtilStats>();

    public SlidingWindowStatsCounter(int slidingWindowMinutes, String name) {
        this.slidingWindowMinutes = slidingWindowMinutes;
        this.name = name;
    }

    public void SlidingWindowStatsCounter(int slidingWindowMinutes) {
        this.slidingWindowMinutes = slidingWindowMinutes;
    }

    public UtilStats incrementAndGetCurrentStats(double sample){
        log.debug(name + "-incrementCounter()");
        long minute = System.currentTimeMillis() / 1000/60;
        if(map.get(minute) == null) {
            log.debug("first data point in this minute :" + minute);
            UtilStats utilStats = new UtilStats();
            utilStats.addSample(sample);
            map.put(minute, utilStats);
            return utilStats;
        }
        else {
            map.get(minute).addSample(sample);
            return map.get(minute);
        }
    }

    public void resetOldCounters(){
        log.debug("resetOldCounters()");
        long minute = System.currentTimeMillis() / 1000/60 - slidingWindowMinutes;
        //log.debug(name + "-resetOldCounters() size is: " + map.size() + " before");
        Iterator<Map.Entry<Long, UtilStats>> iter = map.entrySet().iterator();
        while(iter.hasNext()) {
            Map.Entry<Long, UtilStats> entry = iter.next();
            if(entry.getKey() < minute) {
                log.debug(name + "-remove the counter " + minute);
                map.remove(entry.getKey());
            }
        }
        //log.debug(name + "-resetOldCounters() size is: " + map.size() + " after");
    }

    public synchronized ConcurrentHashMap<Long, UtilStats> advanceAndGetAllStats() {
        log.debug("advanceAndGetAllStats()");
        resetOldCounters();
        return map;
    }

    public void reset() {
        map.clear();
    }
}
