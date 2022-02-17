package com.musicslayer.cryptobuddy.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

public class StreamUtil {
    public static String readIntoString(InputStream inputStream) throws IOException {
        byte[] byteArray = readIntoByteArray(inputStream);
        return new String(byteArray, Charset.forName("UTF-8"));
    }

    public static byte[] readIntoByteArray(InputStream inputStream) throws IOException {
        ByteArrayOutputStream into = null;
        try {
            into = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            for (int n; 0 < (n = inputStream.read(buf));) {
                into.write(buf, 0, n);
            }

            StreamUtil.safeFlushAndClose(into);
            return into.toByteArray();
        }
        catch(IOException e) {
            StreamUtil.safeFlushAndClose(into);
            throw(e);
        }
    }

    public static void writeFromString(OutputStream outputStream, String s) throws IOException {
        byte[] byteArray = s.getBytes(Charset.forName("UTF-8"));
        writeFromByteArray(outputStream, byteArray);
    }

    public static void writeFromByteArray(OutputStream outputStream, byte[] byteArray) throws IOException {
        ByteArrayInputStream from = null;

        try {
            from = new ByteArrayInputStream(byteArray);
            byte[] buf = new byte[4096];
            for (int n; 0 < (n = from.read(buf));) {
                outputStream.write(buf, 0, n);
            }

            StreamUtil.safeFlushAndClose(from);
        }
        catch(IOException e) {
            StreamUtil.safeFlushAndClose(from);
            throw(e);
        }
    }

    public static void safeFlushAndClose(Object obj) {
        try {
            if(obj != null) {
                if(obj instanceof Flushable) {
                    ((Flushable)obj).flush();
                }

                if(obj instanceof Closeable) {
                    ((Closeable)obj).close();
                }
            }
        }
        catch(Exception ignored) {
        }
    }
}