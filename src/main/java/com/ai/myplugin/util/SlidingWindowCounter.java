package com.ai.myplugin.util;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by User: veselin
 * On Date: 27/10/13
 */
public class SlidingWindowCounter {
    private final String name;
    private int slidingWindowMinutes = 5;
    ConcurrentHashMap<Long, AtomicInteger> map = new ConcurrentHashMap<Long, AtomicInteger>();

    public SlidingWindowCounter(int slidingWindowMinutes, String name) {
        this.slidingWindowMinutes = slidingWindowMinutes;
        this.name = name;
    }

    public void setSlidingWindowMinutes(int slidingWindowMinutes) {
        this.slidingWindowMinutes = slidingWindowMinutes;
    }

    public int incrementAndGet(){
        System.out.println(name + "-incrementCounter()");
        long minute = System.currentTimeMillis() / 1000/60;
        if(map.get(minute) == null) {
            System.out.println("first data point in this minute :" + minute);
            map.put(minute, new AtomicInteger(1));
            return 1;
        }
        else {
            return map.get(minute).incrementAndGet();
        }
    }

    public void resetOldCounters(){
        long minute = System.currentTimeMillis() / 1000/60 - slidingWindowMinutes;
        //System.out.println(name + "-resetOldCounters() size is: " + map.size() + " before");
        Iterator<Map.Entry<Long, AtomicInteger>> iter = map.entrySet().iterator();
        while(iter.hasNext()) {
            Map.Entry<Long, AtomicInteger> entry = iter.next();
            if(entry.getKey() < minute) {
                System.out.println(name + "-remove the counter " + minute);
                map.remove(entry.getKey());
            }
        }
        //System.out.println(name + "-resetOldCounters() size is: " + map.size() + " after");
    }

    public synchronized int getTotalCount() {
        resetOldCounters();
        int count = 0;
        for(Map.Entry<Long, AtomicInteger> entry : map.entrySet()){
            count += map.get(entry.getKey()).intValue();
        }
        System.out.println(name + "-getTotalCount() is " + count);
        return count;
    }

    public void reset() {
        map.clear();
    }
}
