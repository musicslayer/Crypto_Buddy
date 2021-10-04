package com.musicslayer.cryptobuddy.util;

import com.musicslayer.cryptobuddy.persistence.Settings;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class REST {
    public static final long limitTime = 1000;
    public static long lastTime = 0;

    // (No one currently uses this, so it is a future item.)
    // Caller should be able to send many times, while only connecting/disconnecting once.
    public static String wss(String urlString, String body) {
        String result = null;
        BasicWebSocketClient client = null;

        try {
            client = new BasicWebSocketClient(new URI(urlString));
            client.connectBlocking(Settings.setting_timeout, TimeUnit.SECONDS);
            client.send(body);

            Date startDate = new Date();
            while(client.RESULT == null) {
                Date endDate = new Date();
                long diff = endDate.getTime() - startDate.getTime(); // milliseconds

                if(diff > Settings.setting_timeout) {
                    break;
                }
            }

            client.closeBlocking();
            result = client.RESULT;
        }
        catch(Exception e) {
            // Try once to close the client, but don't wait for it.
            if(client != null) {
                client.close();
            }

            ThrowableLogger.processThrowable(e);
        }

        return result;
    }

    public static String get(String urlString) {
        // Rate limit - wait a little if we just performed an operation.
        long now = new Date().getTime();
        while(now - lastTime < REST.limitTime) {
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

            result = REST.request(connection);
        }
        catch(Exception e) {
            ThrowableLogger.processThrowable(e);
        }

        return result;
    }

    public static String post(String urlString, String body) {
        // Rate limit - wait a little if we just performed an operation.
        long now = new Date().getTime();
        while(now - lastTime < REST.limitTime) {
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

            result = REST.request(connection);
        }
        catch(Exception e) {
            ThrowableLogger.processThrowable(e);
        }

        return result;
    }

    public static String postWithKey(String urlString, String body, String keyName, String key) {
        // Rate limit - wait a little if we just performed an operation.
        long now = new Date().getTime();
        while(now - lastTime < REST.limitTime) {
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

            result = REST.request(connection);
        }
        catch(Exception e) {
            ThrowableLogger.processThrowable(e);
        }

        return result;
    }

    public static String request(HttpURLConnection connection) {
        final String[] data = new String[1];
        final boolean[] finished = {false};

        Thread thread = new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    InputStream responseStream = connection.getInputStream();
                    String responseString = REST.readString(responseStream);
                    data[0] = responseString;
                }
                catch(IOException e) {
                    data[0] = null;
                }

                finished[0] = true;
            }
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
