package com.musicslayer.cryptobuddy.util;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Environment;

import com.musicslayer.cryptobuddy.BuildConfig;
import com.musicslayer.cryptobuddy.persistence.Persistence;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;

// Methods to dump all app data in a user's installation.
public class DataDumpUtil {
    // Pass in null if we do not have access to the activity object.
    public static String getAllData(Activity a) {
        StringBuilder s = new StringBuilder();
        s.append("Data Dump:");

        // Add all user device data.
        s.append("\n\nDevice Data:");
        s.append(getInfosAboutDevice(a));

        // Add all persistent data.
        s.append("\n\nPersistent Data");

        HashMap<String, HashMap<String, String>> allDataMap = Persistence.getAllData();
        ArrayList<String> allDataKeys = new ArrayList<>(allDataMap.keySet());
        for(String allDataKey : allDataKeys) {
            s.append("\n\n");
            s.append(allDataKey).append(":");

            HashMap<String, String> hashMap = allDataMap.get(allDataKey);

            // If we have no data at all, not even error data, then just skip this map.
            if(hashMap == null) {
                continue;
            }

            // Append data, including error data.
            ArrayList<String> keys = new ArrayList<>(hashMap.keySet());
            for(String key : keys) {
                s.append("\n  ").append(key).append(" = ").append(hashMap.get(key));
            }
        }

        return s.toString();
    }

    public static String getInfosAboutDevice(Activity a) {
        StringBuilder sb = new StringBuilder();

        // application info
        sb.append("\n APP Package Name: ").append(BuildConfig.APPLICATION_ID)
                .append("\n App Version Name: ").append(BuildConfig.VERSION_NAME)
                .append("\n App Version Code: ").append(BuildConfig.VERSION_CODE)
                .append("\n");

        sb.append("\n OS Version: ").append(System.getProperty("os.version")).append(" (").append(android.os.Build.VERSION.INCREMENTAL).append(")")
                .append("\n OS API Level: ").append(android.os.Build.VERSION.SDK)
                .append("\n Device: ").append(android.os.Build.DEVICE)
                .append("\n Model (and Product): ").append(android.os.Build.MODEL).append(" (").append(android.os.Build.PRODUCT).append(")");

        // more from
        // http://developer.android.com/reference/android/os/Build.html :
        sb.append("\n Manufacturer: ").append(android.os.Build.MANUFACTURER)
                .append("\n Other TAGS: ").append(android.os.Build.TAGS)
                .append("\n SD Card state: ").append(Environment.getExternalStorageState());

        if(a != null) {
            sb.append("\n activity: ").append(a.toString())
                    .append("\n screenWidth: ").append(a.getWindow().getWindowManager().getDefaultDisplay().getWidth())
                    .append("\n screenHeight: ").append(a.getWindow().getWindowManager().getDefaultDisplay().getHeight())
                    .append("\n Keyboard available: ").append(a.getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS)
                    .append("\n Trackball available: ").append(a.getResources().getConfiguration().navigation == Configuration.NAVIGATION_TRACKBALL);
        }

        Properties p = System.getProperties();
        Enumeration<Object> keys = p.keys();
        String key;
        while (keys.hasMoreElements()) {
            key = (String) keys.nextElement();
            sb.append("\n > ").append(key).append(" = ").append(p.get(key));
        }

        return sb.toString();
    }
}
