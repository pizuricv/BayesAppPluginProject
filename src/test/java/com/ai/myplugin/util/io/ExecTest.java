package com.ai.myplugin.util.io;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

public class ExecTest {

    @Test(timeout = 1000)
    public void testAwaitTermination() throws Exception {
        Logger logger = LoggerFactory.getLogger("Dummy");

        Process lsProcess = Runtime.getRuntime().exec("ls");
        ExecResult result = Exec.awaitTermination(lsProcess, logger);

        assertEquals(0, result.exitVal);
        System.out.println(result.output);
        assertNotNull(result.output);
    }
}