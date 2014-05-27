package com.ai.myplugin.util.io;

import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class StreamGobbler extends Thread {

    private static final AtomicLong counter = new AtomicLong();

    private final Logger log;
    private final InputStream is;
    private final StdType stdType;
    private final CountDownLatch latch;

    private StringBuffer buffer = new StringBuffer();

    public StreamGobbler(final InputStream is, final StdType type, final Logger log, final CountDownLatch latch) {
        super("StreamGobbler-" + counter.incrementAndGet() + "-" + type);
        this.is = is;
        this.stdType = type;
        this.log = log;
        this.latch = latch;
    }

    public void run() {
        try {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                logLine(line);
                // FIXME this is not 100% correct, might add an extra newline?
                buffer.append(line +  System.lineSeparator());
            }
            latch.countDown();
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    public String getOutput(){
        return buffer.toString().trim();
    }

    private void logLine(String line) {
        if(stdType == StdType.ERROR){
            log.error("Error executing the script > " + line);
        } else{
            log.info(line);
        }
    }
}
