package com.ai.myplugin.util;

import com.ai.myplugin.util.io.IOUtil;
import com.ai.myplugin.util.io.StdType;
import com.ai.myplugin.util.io.StreamGobbler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


public class Node {

    private static final Log log = LogFactory.getLog(Node.class);

    private static final int WAIT_FOR_RESULT_SECONDS = 5;

    private final String workingDir;
    private final String nodePath;

    public Node(final String nodePath, final String workingDir){
        this.nodePath = nodePath;
        this.workingDir = workingDir;
    }

    public NodeResult executeScript(String javaScriptCommand){
        String javascriptFileName =  Long.toString(System.nanoTime()) + "runs.js";

        File dir = new File(workingDir);

        // TODO get the process handling out of here

        File tempScriptFile = new File(dir, javascriptFileName);
        try {
            IOUtil.writeToFile(tempScriptFile, javaScriptCommand);

            ProcessBuilder pb = new ProcessBuilder(nodePath, javascriptFileName);
            pb.directory(dir);
            Process process = pb.start();


            final CountDownLatch countDownLatch = new CountDownLatch(2);
            StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream(), StdType.ERROR, log, countDownLatch);
            StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream(), StdType.OUTPUT, log, countDownLatch);

            errorGobbler.start();
            outputGobbler.start();

            int exitVal = process.waitFor();

            log.debug(" ExitValue: " + exitVal);

            //waitForResult is not a timeout for the javaScriptCommand itself, but how long you wait before the stream of
            //output data is processed, should be really fast.
            countDownLatch.await(WAIT_FOR_RESULT_SECONDS, TimeUnit.SECONDS);
            return new NodeResult(exitVal, outputGobbler.getOutput());
        } catch (IOException t) {
            throw new RuntimeException(t);
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
            // TODO find the best solution
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } finally {
            tempScriptFile.delete();
        }
    }

    public static class NodeResult{
        public final int exitVal;
        public final String output;

        public NodeResult(final int exitVal, final String output){
            this.exitVal = exitVal;
            this.output = output;
        }

    }
}
