package com.musicslayer.cryptobuddy.util;

import com.musicslayer.cryptobuddy.crash.CrashBypassException;
import com.musicslayer.cryptobuddy.settings.TimeoutSetting;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

public class RESTUtil {
    // This is the amount of time we want between web requests, to avoid overloading other APIs and triggering their rate limiting.
    public static final long limitTime = 1000;
    public static final int numRetries = 5;

    // This is static because it needs to be shared between all of the methods.
    // All types of web requests need to be far enough apart from any other type of web request.
    public static long lastTime = 0;

    public static void rateLimit() {
        // Make sure that enough time has elapsed since last call.
        long timeAlreadyElapsed = System.currentTimeMillis() - lastTime;
        PollingUtil.waitFor(RESTUtil.limitTime - timeAlreadyElapsed);
        lastTime = System.currentTimeMillis();
    }

    public static void checkForInterrupt() {
        // If this thread has been interrupted, throw an error so the thread can stop running.
        if(Thread.interrupted()) {
            throw new CrashBypassException();
        }
    }

    public static String get(String urlString) {
        String result = null;

        for(int r = 0; r < numRetries; r++) {
            checkForInterrupt();
            rateLimit();
            result = get_impl(urlString);
            if(result != null) { break; }
        }

        return result;
    }

    private static String get_impl(String urlString) {
        String result = null;

        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("accept", "application/json");

            result = RESTUtil.request(connection);
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
        }

        return result;
    }

    public static String post(String urlString, String body) {
        String result = null;

        for(int r = 0; r < numRetries; r++) {
            checkForInterrupt();
            rateLimit();
            result = post_impl(urlString, body);
            if(result != null) { break; }
        }

        return result;
    }

    private static String post_impl(String urlString, String body) {
        String result = null;

        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");

            byte[] out = body.getBytes(Charset.forName("UTF-8"));
            OutputStream stream = connection.getOutputStream();
            stream.write(out);

            stream.flush();
            stream.close();

            result = RESTUtil.request(connection);
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
        }

        return result;
    }

    public static String postWithKey(String urlString, String body, String keyName, String key) {
        String result = null;

        for(int r = 0; r < numRetries; r++) {
            checkForInterrupt();
            rateLimit();
            result = postWithKey_impl(urlString, body, keyName, key);
            if(result != null) { break; }
        }

        return result;
    }

    private static String postWithKey_impl(String urlString, String body, String keyName, String key) {
        String result = null;

        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty(keyName, key);
            connection.setRequestProperty("Content-Type", "application/json");

            byte[] out = body.getBytes(Charset.forName("UTF-8"));
            OutputStream stream = connection.getOutputStream();
            stream.write(out);

            stream.flush();
            stream.close();

            result = RESTUtil.request(connection);
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
        }

        return result;
    }

    public static String request(HttpURLConnection connection) {
        final String[] data = new String[1];
        final boolean[] finished = {false};

        Thread thread = new Thread(() -> {
            try {
                InputStream responseStream = connection.getInputStream();
                String responseString = RESTUtil.readString(responseStream);
                data[0] = responseString;
            }
            catch(IOException e) {
                data[0] = null;
            }

            finished[0] = true;
        });

        thread.start();
        try {
            thread.join(TimeoutSetting.value);
        }
        catch(InterruptedException e) {
            data[0] = null;
        }

        if(!finished[0]) {
            data[0] = null;
        }

        return data[0];
    }

    private static String readString(InputStream inputStream) throws IOException {
        ByteArrayOutputStream into = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        for (int n; 0 < (n = inputStream.read(buf));) {
            into.write(buf, 0, n);
        }
        into.close();
        return new String(into.toByteArray(), Charset.forName("UTF-8"));
    }
}
