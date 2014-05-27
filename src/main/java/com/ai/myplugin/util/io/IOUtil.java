package com.ai.myplugin.util.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public final class IOUtil {

    private IOUtil(){
        throw new UnsupportedOperationException("Utility class");
    }

    public static void writeToFile(final File tempScriptFile, final String contents) throws IOException {
        try(
                FileWriter fileWriter = new FileWriter(tempScriptFile);
                BufferedWriter output = new BufferedWriter(fileWriter)
        ){
            output.write(contents);
            output.flush();
        }
    }
}
