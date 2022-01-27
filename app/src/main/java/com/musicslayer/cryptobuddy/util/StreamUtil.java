package com.musicslayer.cryptobuddy.util;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class StreamUtil {
    public static String readIntoString(InputStream inputStream) throws IOException {
        ByteArrayOutputStream into = null;
        try {
            into = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            for (int n; 0 < (n = inputStream.read(buf));) {
                into.write(buf, 0, n);
            }

            StreamUtil.safeClose(into);
            return new String(into.toByteArray(), Charset.forName("UTF-8"));
        }
        catch(IOException e) {
            StreamUtil.safeClose(into);
            throw(e);
        }
    }

    public static byte[] readIntoByteArray(InputStream inputStream) throws IOException {
        ByteArrayOutputStream into = null;
        try {
            into = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            for (int n; 0 < (n = inputStream.read(buf));) {
                into.write(buf, 0, n);
            }
            StreamUtil.safeClose(into);
            return into.toByteArray();
        }
        catch(IOException e) {
            StreamUtil.safeClose(into);
            throw(e);
        }
    }

    public static void safeClose(Closeable closeable) {
        try {
            if(closeable != null) {
                closeable.close();
            }
        }
        catch(Exception ignored) {
        }
    }
}