package com.musicslayer.cryptobuddy.util;

import com.musicslayer.cryptobuddy.persistence.Settings;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Date;

public class RESTUtil {
    public static final long limitTime = 1000;
    public static long lastTime = 0;

    public static String get(String urlString) {
        // Rate limit - wait a little if we just performed an operation.
        long now = new Date().getTime();
        while(now - lastTime < RESTUtil.limitTime) {
            now = new Date().getTime();
        }

        lastTime = now;
        return get_impl(urlString);
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
        // Rate limit - wait a little if we just performed an operation.
        long now = new Date().getTime();
        while(now - lastTime < RESTUtil.limitTime) {
            now = new Date().getTime();
        }

        lastTime = now;
        return post_impl(urlString, body);
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
        // Rate limit - wait a little if we just performed an operation.
        long now = new Date().getTime();
        while(now - lastTime < RESTUtil.limitTime) {
            now = new Date().getTime();
        }

        lastTime = now;
        return postWithKey_impl(urlString, body, keyName, key);
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
            thread.join(Settings.setting_timeout);
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
