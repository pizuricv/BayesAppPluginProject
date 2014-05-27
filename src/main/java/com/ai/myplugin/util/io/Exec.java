package com.ai.myplugin.util.io;

import org.slf4j.Logger;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public final class Exec {

    private static final int WAIT_FOR_PROCESS_MINUTES = 5;
    private static final int WAIT_FOR_RESULT_SECONDS = 5;

    private Exec(){
        throw new UnsupportedOperationException("Utility class");
    }

    public static ExecResult awaitTermination(Process process, Logger log) throws IOException, InterruptedException{
        final CountDownLatch countDownLatch = new CountDownLatch(2);
        StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream(), StdType.ERROR, log, countDownLatch);
        StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream(), StdType.OUTPUT, log, countDownLatch);

        errorGobbler.start();
        outputGobbler.start();

        boolean done = process.waitFor(WAIT_FOR_PROCESS_MINUTES, TimeUnit.MINUTES);
        if(!done){
            process.destroy();
            throw new IOException("External process timed out at " + WAIT_FOR_PROCESS_MINUTES + " minutes");
        }
        int exitVal = process.exitValue();
        log.debug(" ExitValue: " + exitVal);

        //waitForResult is not a timeout for the javaScriptCommand itself, but how long you wait before the stream of
        //output data is processed, should be really fast.
        countDownLatch.await(WAIT_FOR_RESULT_SECONDS, TimeUnit.SECONDS);
        return new ExecResult(exitVal, outputGobbler.getOutput());
    }
}
