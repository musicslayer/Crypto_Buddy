package com.musicslayer.cryptobuddy.util;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;

import com.musicslayer.cryptobuddy.dialog.ProgressDialogFragment;
import com.musicslayer.cryptobuddy.settings.setting.TimeoutSetting;

import org.apache.commons.io.FileUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class WebUtil {
    // This is the amount of time we want between web requests, to avoid overloading other APIs and triggering their rate limiting.
    public static final long limitTime = 1000;
    public static final int numRetries = 5;

    // This is static because it needs to be shared between all of the methods.
    // All types of web requests need to be far enough apart from any other type of web request.
    public static long lastTime = 0;

    public static void rateLimit() {
        // Make sure that enough time has elapsed since last call.
        long timeAlreadyElapsed = System.currentTimeMillis() - lastTime;
        PollingUtil.waitFor(WebUtil.limitTime - timeAlreadyElapsed);
        lastTime = System.currentTimeMillis();
    }

    public static String get(String urlString) {
        String result = null;

        for(int r = 0; r < numRetries; r++) {
            ProgressDialogFragment.checkForInterrupt();
            rateLimit();
            result = get_impl(urlString);
            if(result != null) { break; }
        }

        return result;
    }

    private static String get_impl(String urlString) {
        String result = null;

        HttpURLConnection connection = null;

        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("accept", "application/json");

            result = WebUtil.request(connection);

            safeDisconnect(connection);
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            safeDisconnect(connection);
        }

        return result;
    }

    public static String getWithToken(String urlString, String token) {
        String result = null;

        for(int r = 0; r < numRetries; r++) {
            ProgressDialogFragment.checkForInterrupt();
            rateLimit();
            result = getWithToken_impl(urlString, token);
            if(result != null) { break; }
        }

        return result;
    }

    private static String getWithToken_impl(String urlString, String token) {
        String result = null;

        HttpURLConnection connection = null;

        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("accept", "application/json");

            // TODO These are Coinbase specific. We really should have a way of inputting these.
            connection.setRequestProperty("Authorization", "Bearer " + token);
            connection.setRequestProperty("CB-VERSION", "2022-03-04");

            result = WebUtil.request(connection);

            safeDisconnect(connection);
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            safeDisconnect(connection);
        }

        return result;
    }

    public static String post(String urlString, String body) {
        String result = null;

        for(int r = 0; r < numRetries; r++) {
            ProgressDialogFragment.checkForInterrupt();
            rateLimit();
            result = post_impl(urlString, body);
            if(result != null) { break; }
        }

        return result;
    }

    private static String post_impl(String urlString, String body) {
        String result = null;

        OutputStream stream = null;
        HttpURLConnection connection = null;

        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");

            stream = connection.getOutputStream();
            StreamUtil.writeFromString(stream, body);
            StreamUtil.safeFlushAndClose(stream);

            result = WebUtil.request(connection);

            safeDisconnect(connection);
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            StreamUtil.safeFlushAndClose(stream);
            safeDisconnect(connection);
        }

        return result;
    }

    public static String postWithKey(String urlString, String body, String keyName, String key) {
        String result = null;

        for(int r = 0; r < numRetries; r++) {
            ProgressDialogFragment.checkForInterrupt();
            rateLimit();
            result = postWithKey_impl(urlString, body, keyName, key);
            if(result != null) { break; }
        }

        return result;
    }

    private static String postWithKey_impl(String urlString, String body, String keyName, String key) {
        String result = null;

        OutputStream stream = null;
        HttpURLConnection connection = null;

        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty(keyName, key);
            connection.setRequestProperty("Content-Type", "application/json");

            stream = connection.getOutputStream();
            StreamUtil.writeFromString(stream, body);
            StreamUtil.safeFlushAndClose(stream);

            result = WebUtil.request(connection);

            safeDisconnect(connection);
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            StreamUtil.safeFlushAndClose(stream);
            safeDisconnect(connection);
        }

        return result;
    }

    public static boolean download(String urlString, File file) {
        // Returns whether or not the file was successfully written to.
        boolean result = false;

        for(int r = 0; r < numRetries; r++) {
            ProgressDialogFragment.checkForInterrupt();
            rateLimit();
            result = download_impl(urlString, file);
            if(result) { break; }
        }

        return result;
    }

    private static boolean download_impl(String urlString, File file) {
        boolean result = false;

        InputStream responseStream = null;

        try {
            URL url = new URL(urlString);

            responseStream = new BufferedInputStream(url.openStream());
            byte[] responseByteArray = StreamUtil.readIntoByteArray(responseStream);
            StreamUtil.safeFlushAndClose(responseStream);

            FileUtils.writeByteArrayToFile(file, responseByteArray);
            result = true;
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            StreamUtil.safeFlushAndClose(responseStream);
        }

        return result;
    }

    public static String request(HttpURLConnection connection) {
        final String[] data = new String[1];
        final boolean[] finished = {false};

        Thread thread = new Thread(() -> {
            InputStream responseStream = null;

            try {
                responseStream = connection.getInputStream();
                String responseString = StreamUtil.readIntoString(responseStream);
                StreamUtil.safeFlushAndClose(responseStream);
                data[0] = responseString;
            }
            catch(IOException e) {
                StreamUtil.safeFlushAndClose(responseStream);
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

    public static void launchURL(Activity activity, String url) {
        Intent webIntent = new Intent(Intent.ACTION_VIEW);
        webIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY); // Used so that we can close the web browser window programmatically.
        webIntent.setData(Uri.parse(url));

        ComponentName webApp = webIntent.resolveActivity(activity.getPackageManager());
        ComponentName unsupportedAction = ComponentName.unflattenFromString("com.android.fallback/.Fallback");
        if(webApp != null && !webApp.equals(unsupportedAction)) {
            activity.startActivity(webIntent);
        }
        else {
            ToastUtil.showToast("web_browser");
        }
    }

    public static void safeDisconnect(HttpURLConnection urlConnection) {
        try {
            if(urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        catch(Exception ignored) {
        }
    }
}
