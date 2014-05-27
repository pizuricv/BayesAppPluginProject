package com.ai.myplugin.util.io;

import com.ai.myplugin.action.NodeJSAction;
import org.apache.commons.logging.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public class StreamGobbler extends Thread {

    private final Log log;
    private final InputStream is;
    private final StdType stdType;
    private final CountDownLatch latch;

    private StringBuffer buffer = new StringBuffer();

    public StreamGobbler(final InputStream is, final StdType type, final Log log, final CountDownLatch latch) {
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
                logLine(line, stdType);
            }
            latch.countDown();
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    public String getOutput(){
        return buffer.toString();
    }

    private void logLine(String line, StdType type) {
        if(type == StdType.ERROR){
            log.error("Error executing the script > " + line);
        } else{
            log.info(line);
        }
        buffer.append(line);
    }
}
