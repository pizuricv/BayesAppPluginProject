package com.ai.myplugin.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by User: veselin
 * On Date: 26/03/14
 */
public class SlidingWindowStatsCounter {
    private static final Logger log = LoggerFactory.getLogger(SlidingWindowStatsCounter.class);
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

    public UtilStats getCurrentStats() {
        log.debug(name + "-getCurrentStats()");
        ConcurrentHashMap<Long, UtilStats> map = advanceAndGetAllStats();
        UtilStats sumStats = new UtilStats();
        double count =0, sum = 0;
        for(UtilStats utilStats : map.values()){
            if(sumStats.max < utilStats.max)
                sumStats.max = utilStats.max;
            if(sumStats.min > utilStats.min)
                sumStats.min = utilStats.min;
            count += utilStats.n;
            sum += utilStats.avg * utilStats.n;
        }
        if(count > 0)
            sumStats.avg = sum/count;
        sumStats.n = (int)count;
        return sumStats;
    }

    public UtilStats incrementAndGetStatsForMinute(long minute, double sample){
        log.debug(name + "-incrementAndGetCurrentStats(), minute["+minute+"] "+sample);
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
    public UtilStats incrementAndGetStatsForCurrentMinute(double sample){
        long minute = System.currentTimeMillis() / 1000/60;
        return incrementAndGetStatsForMinute(minute, sample);
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
