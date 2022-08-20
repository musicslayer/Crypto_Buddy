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
import java.util.ArrayList;

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
        ArrayList<String> keyNameArrayList = new ArrayList<>();
        ArrayList<String> keyArrayList = new ArrayList<>();
        return get(urlString, keyNameArrayList, keyArrayList);
    }

    public static String get(String urlString, String keyName, String key) {
        ArrayList<String> keyNameArrayList = new ArrayList<>();
        keyNameArrayList.add(keyName);

        ArrayList<String> keyArrayList = new ArrayList<>();
        keyArrayList.add(key);

        return get(urlString, keyNameArrayList, keyArrayList);
    }

    public static String get(String urlString, ArrayList<String> keyNameArrayList, ArrayList<String> keyArrayList) {
        String result = null;

        for(int r = 0; r < numRetries; r++) {
            ProgressDialogFragment.checkForInterrupt();
            rateLimit();
            result = get_impl(urlString, keyNameArrayList, keyArrayList);
            if(result != null) { break; }
        }

        return result;
    }

    private static String get_impl(String urlString, ArrayList<String> keyNameArrayList, ArrayList<String> keyArrayList) {
        String result = null;

        HttpURLConnection connection = null;

        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("accept", "application/json");

            // Assume arraylists are non-null and the same size.
            for(int i = 0; i < keyNameArrayList.size(); i++) {
                connection.setRequestProperty(keyNameArrayList.get(i), keyArrayList.get(i));
            }

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
        ArrayList<String> keyNameArrayList = new ArrayList<>();
        ArrayList<String> keyArrayList = new ArrayList<>();
        return post(urlString, body, keyNameArrayList, keyArrayList);
    }

    public static String post(String urlString, String body, String keyName, String key) {
        ArrayList<String> keyNameArrayList = new ArrayList<>();
        keyNameArrayList.add(keyName);

        ArrayList<String> keyArrayList = new ArrayList<>();
        keyArrayList.add(key);

        return post(urlString, body, keyNameArrayList, keyArrayList);
    }

    public static String post(String urlString, String body, ArrayList<String> keyNameArrayList, ArrayList<String> keyArrayList) {
        String result = null;

        for(int r = 0; r < numRetries; r++) {
            ProgressDialogFragment.checkForInterrupt();
            rateLimit();
            result = post_impl(urlString, body, keyNameArrayList, keyArrayList);
            if(result != null) { break; }
        }

        return result;
    }

    private static String post_impl(String urlString, String body, ArrayList<String> keyNameArrayList, ArrayList<String> keyArrayList) {
        String result = null;

        OutputStream stream = null;
        HttpURLConnection connection = null;

        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");

            // Assume arraylists are non-null and the same size.
            for(int i = 0; i < keyNameArrayList.size(); i++) {
                connection.setRequestProperty(keyNameArrayList.get(i), keyArrayList.get(i));
            }

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
            catch(IOException ignored) {
                StreamUtil.safeFlushAndClose(responseStream);
                data[0] = null;
            }

            finished[0] = true;
        });

        thread.start();
        try {
            thread.join(TimeoutSetting.value);
        }
        catch(InterruptedException ignored) {
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
