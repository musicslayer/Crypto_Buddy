package com.musicslayer.cryptobuddy.persistence;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

import com.musicslayer.cryptobuddy.json.JSONWithNull;
import com.musicslayer.cryptobuddy.data.Serialization;
import com.musicslayer.cryptobuddy.settings.setting.Setting;
import com.musicslayer.cryptobuddy.util.ContextUtil;

public class SettingList {
    // Just pick something that would never actually be saved.
    public final static String DEFAULT = "!UNKNOWN!";

    public static String getSharedPreferencesKey() {
        return "settings_data";
    }

    public static void saveSetting(Context context, Setting setting) {
        SharedPreferences settings = context.getSharedPreferences(getSharedPreferencesKey(), MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.putString("settings_" + setting.getSettingsKey(), Serialization.serialize(setting));
        editor.apply();
    }

    public static void saveAllSettings(Context context) {
        SharedPreferences settings = context.getSharedPreferences(getSharedPreferencesKey(), MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        for(Setting setting : Setting.settings) {
            editor.putString("settings_" + setting.getSettingsKey(), Serialization.serialize(setting));
        }

        editor.apply();
    }

    public static Setting loadData(Context context, String settingsKey) {
        // Setting will create empty objects, but then this method will fill them in with data.
        // If a new Setting is introduced later, it will still be created but will get no data from here.
        SharedPreferences settings = context.getSharedPreferences(getSharedPreferencesKey(), MODE_PRIVATE);
        String serialString = settings.getString("settings_" + settingsKey, DEFAULT);

        return DEFAULT.equals(serialString) ? null : Serialization.deserialize(serialString, Setting.class);
    }

    public static void resetAllData(Context context) {
        SharedPreferences settings = context.getSharedPreferences(getSharedPreferencesKey(), MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.clear();
        editor.apply();
    }

    // TODO New interface???
    //public String exportationVersion() { return "1"; }

    public static boolean canExport() { return true; }

    public static String exportToJSON(Context context) throws org.json.JSONException {
        SharedPreferences settings = context.getSharedPreferences(getSharedPreferencesKey(), MODE_PRIVATE);

        JSONWithNull.JSONObjectWithNull o = new JSONWithNull.JSONObjectWithNull();

        for(Setting setting : Setting.settings) {
            String key = "settings_" + setting.getSettingsKey();
            String serialString = settings.getString(key, DEFAULT);
            o.put(key, serialString);
        }

        return o.toStringOrNull();
    }


    public static void importFromJSON1(Context context, String s) throws org.json.JSONException {
        JSONWithNull.JSONObjectWithNull o = new JSONWithNull.JSONObjectWithNull(s);

        // Only import settings that currently exist.
        SharedPreferences settings = context.getSharedPreferences(getSharedPreferencesKey(), MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        for(Setting setting : Setting.settings) {
            String key = "settings_" + setting.getSettingsKey();
            if(o.has(key)) {
                String value = o.getString(key);
                if(!DEFAULT.equals(value)) {
                    editor.putString(key, Serialization.validate(value, Setting.class));
                }
            }
        }

        editor.apply();

        // Reinitialize data. Some settings need to recreate the activity, so do it unconditionally just to be safe.
        Setting.initialize(context);
        ContextUtil.getActivityFromContext(context).recreate();
    }
}
