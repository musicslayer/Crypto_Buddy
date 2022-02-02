package com.musicslayer.cryptobuddy.persistence;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;

import static android.content.Context.MODE_PRIVATE;

import com.musicslayer.cryptobuddy.serialize.Serialization;
import com.musicslayer.cryptobuddy.settings.setting.Setting;
import com.musicslayer.cryptobuddy.util.ThrowableUtil;

public class SettingList {
    // Just pick something that would never actually be saved.
    public final static String DEFAULT = "!UNKNOWN!";

    public static void saveSetting(Context context, Setting setting) {
        SharedPreferences settings = context.getSharedPreferences("settings_data", MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.putString("settings_" + setting.getSettingsKey(), Serialization.serialize(setting));
        editor.apply();
    }

    public static void saveAllSettings(Context context) {
        SharedPreferences settings = context.getSharedPreferences("settings_data", MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        for(Setting setting : Setting.settings) {
            editor.putString("settings_" + setting.getSettingsKey(), Serialization.serialize(setting));
        }

        editor.apply();
    }

    public static Setting loadData(Context context, String settingsKey) {
        // Setting will create empty objects, but then this method will fill them in with data.
        // If a new Setting is introduced later, it will still be created but will get no data from here.
        SharedPreferences settings = context.getSharedPreferences("settings_data", MODE_PRIVATE);
        String serialString = settings.getString("settings_" + settingsKey, DEFAULT);

        return DEFAULT.equals(serialString) ? null : Serialization.deserialize(serialString, Setting.class);
    }

    public static HashMap<String, String> getAllData() {
        HashMap<String, String> hashMap = new HashMap<>();

        try {
            for(Setting setting : Setting.settings) {
                hashMap.put(setting.getSettingsKey(), Serialization.serialize(setting));
            }
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
        }

        return hashMap;
    }

    public static void resetAllData(Context context) {
        SharedPreferences settings = context.getSharedPreferences("settings_data", MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.clear();
        editor.apply();
    }
}
