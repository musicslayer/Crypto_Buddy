package com.musicslayer.cryptobuddy.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.musicslayer.cryptobuddy.app.App;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SharedPreferencesUtil {
    public static SharedPreferences getSharedPreferences(String name) {
        return App.applicationContext.getSharedPreferences(name, Context.MODE_PRIVATE);
    }

    public static ArrayList<String> getDataKeys(String name) {
        // Get keys to all data inside a SharedPreferences instance.
        HashMap<String, String> hashMap = new HashMap<>();

        SharedPreferences settings = getSharedPreferences(name);
        Map<String, ?> settingsMap = settings.getAll();
        return new ArrayList<>(settingsMap.keySet());
    }

    public static HashMap<String, String> getDataMap(String name) {
        // Get all data inside a SharedPreferences instance as a HashMap.
        // All values are converted to Strings, since this is used for data dumps.
        HashMap<String, String> hashMap = new HashMap<>();

        SharedPreferences settings = getSharedPreferences(name);
        Map<String, ?> settingsMap = settings.getAll();
        for(String key : settingsMap.keySet()) {
            Object value = settingsMap.get(key);
            String valueString = value == null ? "null" : value.toString();
            hashMap.put(key, valueString);
        }

        return hashMap;
    }
}
