package com.ai.myplugin.util.io;

import java.io.*;
import java.util.Optional;

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

    public static String readToString(InputStream inputStream, Optional<String> charsetName) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        pipe(inputStream, bos);
        return bos.toString(charsetName.orElse("utf-8"));
    }

    public static String readToString(InputStream inputStream, String charsetName) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        pipe(inputStream, bos);
        return bos.toString(charsetName);
    }

    public static void pipe(InputStream is, OutputStream os) throws IOException {
        int n;
        byte[] buffer = new byte[1024];
        while ((n = is.read(buffer)) > -1) {
            os.write(buffer, 0, n);
        }
    }

    /**
     * Read the Stream content as a string (use utf-8)
     * @param is The stream to read
     * @return The String content
     */
    public static String readContentAsString(InputStream is) {
        return readContentAsString(is, "utf-8");
    }

    /**
     * Read the Stream content as a string
     * @param is The stream to read
     * @return The String content
     */
    public static String readContentAsString(InputStream is, String encoding) {
        String res = null;
        try {
            res = readToString(is, encoding);
        } catch(Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                is.close();
            } catch(Exception e) {
                //
            }
        }
        return res;
    }


    public static String readFromClasspath(Class context, String file) throws IOException {
        try(InputStream is = context.getResourceAsStream(file)) {
            return readContentAsString(is);
        }
    }
}
