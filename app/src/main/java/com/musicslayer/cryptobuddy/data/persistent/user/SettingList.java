package com.musicslayer.cryptobuddy.data.persistent.user;

import android.content.SharedPreferences;

import com.musicslayer.cryptobuddy.data.bridge.DataBridge;
import com.musicslayer.cryptobuddy.data.bridge.Exportation;
import com.musicslayer.cryptobuddy.data.bridge.Serialization;
import com.musicslayer.cryptobuddy.settings.setting.Setting;
import com.musicslayer.cryptobuddy.util.SharedPreferencesUtil;

public class SettingList extends PersistentUserDataStore implements Exportation.ExportableToJSON, Exportation.Versionable {
    public String getName() { return "SettingList"; }

    public boolean canExport() { return true; }
    public String doExport() { return Exportation.exportData(this, SettingList.class); }
    public void doImport(String s) { Exportation.importData(this, s, SettingList.class); }

    // Just pick something that would never actually be saved.
    public final static String DEFAULT = "!UNKNOWN!";

    public String getSharedPreferencesKey() {
        return "settings_data";
    }

    public void saveSetting(Setting setting) {
        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("settings_" + setting.getSettingsKey(), Serialization.serialize(setting, Setting.class));
        editor.apply();
    }

    public void saveAllData() {
        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        for(Setting setting : Setting.settings) {
            editor.putString("settings_" + setting.getSettingsKey(), Serialization.serialize(setting, Setting.class));
        }

        editor.apply();
    }

    public void loadAllData() {
        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());

        // For each Setting, look for any stored data to fill in.
        for(Setting setting : Setting.settings) {
            String serialString = sharedPreferences.getString("settings_" + setting.getSettingsKey(), DEFAULT);

            String optionName;
            Setting copySetting = DEFAULT.equals(serialString) ? null : Serialization.deserialize(serialString, Setting.class);
            if(copySetting != null && setting.getOptionNames().contains(copySetting.chosenOptionName)) {
                optionName = copySetting.chosenOptionName;
            }
            else {
                optionName = setting.getDefaultOptionName();
            }

            setting.setSetting(optionName);
        }
    }

    public void resetAllData() {
        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.clear();
        editor.apply();
    }

    public static String exportationVersion() {
        return "1";
    }

    public static String exportationType(String version) {
        return "!OBJECT!";
    }

    public String exportDataToJSON() throws org.json.JSONException {
        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());

        DataBridge.JSONObjectDataBridge o = new DataBridge.JSONObjectDataBridge();

        for(Setting setting : Setting.settings) {
            String key = "settings_" + setting.getSettingsKey();
            String serialString = sharedPreferences.getString(key, DEFAULT);
            o.serialize(key, serialString, String.class);
        }

        return o.toStringOrNull();
    }


    public void importDataFromJSON(String s, String version) throws org.json.JSONException {
        DataBridge.JSONObjectDataBridge o = new DataBridge.JSONObjectDataBridge(s);

        // Only import settings that currently exist.
        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        for(Setting setting : Setting.settings) {
            String key = "settings_" + setting.getSettingsKey();
            if(o.has(key)) {
                String value = o.deserialize(key, String.class);
                if(!DEFAULT.equals(value)) {
                    editor.putString(key, Serialization.cycle(value, Setting.class));
                }
            }
        }

        editor.apply();

        // Reinitialize data.
        initialize();
    }
}
