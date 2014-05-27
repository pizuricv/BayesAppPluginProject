package com.ai.myplugin.util.io;

import java.io.*;

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

    public static String readToString(InputStream inputStream) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        pipe(inputStream, bos);
        return bos.toString();
    }

    public static void pipe(InputStream is, OutputStream os) throws IOException {
        int n;
        byte[] buffer = new byte[1024];
        while ((n = is.read(buffer)) > -1) {
            os.write(buffer, 0, n);
        }
    }
}
