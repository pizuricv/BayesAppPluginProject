package com.ai.myplugin.util.io;

/**
* Created by francisdb on 27/05/14.
*/
public class ExecResult {
    public final int exitVal;
    public final String output;

    public ExecResult(final int exitVal, final String output){
        this.exitVal = exitVal;
        this.output = output;
    }

}
