package com.ai.myplugin.util;

import com.ai.myplugin.util.io.Exec;
import com.ai.myplugin.util.io.ExecResult;
import com.ai.myplugin.util.io.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;


public class Node {

    private static final Logger log = LoggerFactory.getLogger(Node.class);

    private final String workingDir;
    private final String nodePath;

    public Node(final String nodePath, final String workingDir){
        this.nodePath = nodePath;
        this.workingDir = workingDir;
    }

    public ExecResult executeScript(String javaScriptCommand){
        String javascriptFileName =  Long.toString(System.nanoTime()) + "runs.js";

        File dir = new File(workingDir);

        // TODO get the process handling out of here

        File tempScriptFile = new File(dir, javascriptFileName);
        try {
            IOUtil.writeToFile(tempScriptFile, javaScriptCommand);

            ProcessBuilder pb = new ProcessBuilder(nodePath, javascriptFileName);
            pb.directory(dir);
            Process process = pb.start();

            return Exec.awaitTermination(process, log);

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

}
