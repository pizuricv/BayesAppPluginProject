package com.ai.myplugin.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.StringTokenizer;

/**
 * Created by User: veselin
 * On Date: 26/03/14
 */
public class SequenceStats {

    private static final Logger log = LoggerFactory.getLogger(SequenceStats.class);

    private LinkedList samples;
    private String sequence="|";
    private int length;

    public SequenceStats(String sequence) {
        log.info("init SequenceStats with sequence " +sequence);
        StringTokenizer stringTokenizer = new StringTokenizer(sequence,",");
        length = stringTokenizer.countTokens();
        log.info("Sequence size: " + length);
        if(length < 1)
            throw new RuntimeException("sequence: " + sequence + " not correct");
        while(stringTokenizer.hasMoreElements())
            this.sequence += stringTokenizer.nextToken().trim() + "|";
        samples = new LinkedList();
    }

    public synchronized void addSample(String sample){
        log.info("add sample "+sample);
        if(samples.size() >= length)  {
            samples.removeLast();

        }
        samples.push(sample);
        log.info("new values are: "+toString());
    }

    @Override
    public String toString() {
        return "SequenceStats{" +
                "samples=" + samples +
                ", sequence='" + sequence + '\'' +
                '}';
    }

    public boolean isMatching() {
        log.info("isMatching " + toString());
        String listSequence = "|";
        for(int i =samples.size()-1; i>-1; i--){
            listSequence += samples.get(i) + "|";
        }
        return listSequence.equals(sequence);
    }
}