package com.musicslayer.cryptobuddy.persistence;

import android.content.SharedPreferences;

import com.musicslayer.cryptobuddy.data.Exportation;
import com.musicslayer.cryptobuddy.json.JSONWithNull;
import com.musicslayer.cryptobuddy.data.Serialization;
import com.musicslayer.cryptobuddy.settings.setting.Setting;
import com.musicslayer.cryptobuddy.util.SharedPreferencesUtil;

public class SettingList implements Exportation.ExportableToJSON, Exportation.Versionable {
    // Just pick something that would never actually be saved.
    public final static String DEFAULT = "!UNKNOWN!";

    public static String getSharedPreferencesKey() {
        return "settings_data";
    }

    public static void saveSetting(Setting setting) {
        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("settings_" + setting.getSettingsKey(), Serialization.serialize(setting, Setting.class));
        editor.apply();
    }

    public static void saveAllSettings() {
        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        for(Setting setting : Setting.settings) {
            editor.putString("settings_" + setting.getSettingsKey(), Serialization.serialize(setting, Setting.class));
        }

        editor.apply();
    }

    public static Setting loadData(String settingsKey) {
        // Setting will create empty objects, but then this method will fill them in with data.
        // If a new Setting is introduced later, it will still be created but will get no data from here.
        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        String serialString = sharedPreferences.getString("settings_" + settingsKey, DEFAULT);

        return DEFAULT.equals(serialString) ? null : Serialization.deserialize(serialString, Setting.class);
    }

    public static void resetAllData() {
        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.clear();
        editor.apply();
    }

    public static boolean canExport() {
        return true;
    }

    public String exportationVersion() {
        return "1";
    }

    public static String exportDataToJSON() throws org.json.JSONException {
        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());

        JSONWithNull.JSONObjectWithNull o = new JSONWithNull.JSONObjectWithNull();

        for(Setting setting : Setting.settings) {
            String key = "settings_" + setting.getSettingsKey();
            String serialString = sharedPreferences.getString(key, DEFAULT);
            o.put(key, serialString, String.class);
        }

        return o.toStringOrNull();
    }


    public static void importDataFromJSON(String s, String version) throws org.json.JSONException {
        JSONWithNull.JSONObjectWithNull o = new JSONWithNull.JSONObjectWithNull(s);

        // Only import settings that currently exist.
        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        for(Setting setting : Setting.settings) {
            String key = "settings_" + setting.getSettingsKey();
            if(o.has(key)) {
                String value = o.get(key, String.class);
                if(!DEFAULT.equals(value)) {
                    editor.putString(key, Serialization.validate(value, Setting.class));
                }
            }
        }

        editor.apply();

        // Reinitialize data. Some settings need to recreate the activity, so do it unconditionally just to be safe.
        Setting.initialize();

        // TODO How to get the real activity.
        //ContextUtil.getActivityFromContext(context).recreate();
    }
}
