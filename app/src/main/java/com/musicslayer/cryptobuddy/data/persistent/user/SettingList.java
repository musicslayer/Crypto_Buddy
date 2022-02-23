package com.musicslayer.cryptobuddy.data.persistent.user;

import android.content.SharedPreferences;

import com.musicslayer.cryptobuddy.data.bridge.DataBridge;
import com.musicslayer.cryptobuddy.settings.setting.Setting;
import com.musicslayer.cryptobuddy.settings.setting.UnknownSetting;
import com.musicslayer.cryptobuddy.util.SharedPreferencesUtil;

import java.io.IOException;

public class SettingList extends PersistentUserDataStore implements DataBridge.ExportableToJSON {
    public String getName() { return "SettingList"; }

    public boolean isVisible() { return true; }
    public String doExport() { return DataBridge.exportData(this, SettingList.class); }
    public void doImport(String s) { DataBridge.importData(this, s, SettingList.class); }

    // Just pick something that would never actually be saved.
    public final static String DEFAULT = "!UNKNOWN!";

    public String getSharedPreferencesKey() {
        return "settings_data";
    }

    public void saveSetting(Setting setting) {
        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("settings_" + setting.getSettingsKey(), DataBridge.serialize(setting, Setting.class));
        editor.apply();
    }

    public void saveAllData() {
        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        for(Setting setting : Setting.settings) {
            editor.putString("settings_" + setting.getSettingsKey(), DataBridge.serialize(setting, Setting.class));
        }

        editor.apply();
    }

    public void loadAllData() {
        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());

        // For each Setting, look for any stored data to fill in.
        for(Setting setting : Setting.settings) {
            String serialString = sharedPreferences.getString("settings_" + setting.getSettingsKey(), DEFAULT);

            String optionName;
            Setting copySetting = DEFAULT.equals(serialString) ? null : DataBridge.deserialize(serialString, Setting.class);
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

        loadAllData();
    }

    @Override
    public void exportDataToJSON(DataBridge.Writer o) throws IOException {
        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());

        o.beginObject();
        o.serialize("!V!", "1", String.class);

        for(String key : SharedPreferencesUtil.getDataKeys(getSharedPreferencesKey())) {
            String serialString = sharedPreferences.getString(key, DEFAULT);
            o.serialize(key, serialString, String.class);
        }

        o.endObject();
    }


    @Override
    public void importDataFromJSON(DataBridge.Reader o) throws IOException {
        o.beginObject();

        String version = o.deserialize("!V!", String.class);
        if(!"1".equals(version)) {
            throw new IllegalStateException();
        }

        // Only import settings that currently exist.
        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        while(o.jsonReader.hasNext()) {
            String key = o.getName();
            String value = o.getString();
            String settings_key = key.replace("settings_", "");

            Setting setting = Setting.getSettingFromSettingsKey(settings_key);
            if(!(setting instanceof UnknownSetting) && !DEFAULT.equals(value)) {
                editor.putString(key, DataBridge.cycleSerialization(value, Setting.class));
            }
        }

        editor.apply();

        o.endObject();

        // Reinitialize data.
        loadAllData();
    }
}
