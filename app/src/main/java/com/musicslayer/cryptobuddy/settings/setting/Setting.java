package com.musicslayer.cryptobuddy.settings.setting;

import android.content.Context;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.persistence.SettingList;
import com.musicslayer.cryptobuddy.serialize.Serialization;
import com.musicslayer.cryptobuddy.util.FileUtil;
import com.musicslayer.cryptobuddy.util.ReflectUtil;
import com.musicslayer.cryptobuddy.view.settings.SettingsView;
import com.musicslayer.cryptobuddy.view.settings.StandardSettingsView;

import java.util.ArrayList;
import java.util.HashMap;

abstract public class Setting implements Serialization.SerializableToJSON {
    public static ArrayList<Setting> settings;
    public static HashMap<String, Setting> setting_map;
    public static HashMap<String, Setting> setting_settings_map;
    public static ArrayList<String> setting_names;
    public static ArrayList<String> setting_display_names;

    public int chosenOptionPosition;
    public String chosenOptionName;

    abstract public String getKey();
    abstract public String getName();
    abstract public String getDisplayName();

    // Used to store persistent state
    abstract public String getSettingsKey();

    // These have all the options a user can choose.
    abstract public ArrayList<String> getOptionNames();
    abstract public String getDefaultOptionName();
    abstract public ArrayList<String> getOptionDisplays();
    abstract public <T> ArrayList<T> getOptionValues();
    abstract public void updateValue();

    public void setSetting(int chosenOptionPosition) {
        this.chosenOptionPosition = chosenOptionPosition;
        this.chosenOptionName = getOptionNames().get(chosenOptionPosition);
        updateValue();
    }

    // Usually this is not the case, but when some settings are changed we must recreate the Activity.
    public boolean needsRefresh() { return false; }

    // Can be used to modify how we display options with extra data that we don't want to store (like the time zone offset).
    public String modifyOptionName(String optionName) { return optionName; }

    public ArrayList<String> getModifiedOptionNames() {
        ArrayList<String> modifiedOptionNames = new ArrayList<>();
        for(String optionName : getOptionNames()) {
            modifiedOptionNames.add(modifyOptionName(optionName));
        }
        return modifiedOptionNames;
    }

    // Can be overridden to return a tailored View for this setting.
    public SettingsView createSettingView(Context context) {
        return new StandardSettingsView(context, this);
    }

    public static void initialize(Context context) {
        settings = new ArrayList<>();
        setting_map = new HashMap<>();
        setting_settings_map = new HashMap<>();
        setting_display_names = new ArrayList<>();

        setting_names = FileUtil.readFileIntoLines(context, R.raw.settings_setting);
        for(String settingName : setting_names) {
            Setting setting = ReflectUtil.constructClassInstanceFromName("com.musicslayer.cryptobuddy.settings.setting." + settingName);
            Setting copySetting = SettingList.loadData(context, setting.getSettingsKey());

            int idx;
            if(copySetting == null) {
                // If this is a new setting, use default value.
                idx = setting.getOptionNames().indexOf(setting.getDefaultOptionName());
            }
            else {
                idx = setting.getOptionNames().indexOf(copySetting.chosenOptionName);
                if(idx == -1) {
                    // If saved option choice no longer exists, use default value.
                    idx = setting.getOptionNames().indexOf(setting.getDefaultOptionName());
                }
            }

            setting.setSetting(idx);

            settings.add(setting);
            setting_map.put(settingName, setting);
            setting_settings_map.put(setting.getSettingsKey(), setting);
            setting_display_names.add(setting.getDisplayName());
        }
    }

    public static Setting getSettingFromKey(String key) {
        Setting setting = setting_map.get(key);
        if(setting == null) {
            setting = UnknownSetting.createUnknownSetting(key, "?");
        }

        return setting;
    }

    public static void resetAllSettings() {
        // Resets all settings to their default values.
        for(Setting setting : settings) {
            int idx = setting.getOptionNames().indexOf(setting.getDefaultOptionName());
            setting.setSetting(idx);
        }
    }

    public String serializationVersion() { return "1"; }

    public String serializeToJSON() throws org.json.JSONException {
        return new Serialization.JSONObjectWithNull()
            .put("key", Serialization.string_serialize(getKey()))
            .put("settingsKey", Serialization.string_serialize(getSettingsKey()))
            .put("chosenOptionPosition", Serialization.int_serialize(chosenOptionPosition))
            .put("chosenOptionName", Serialization.string_serialize(chosenOptionName))
            .toStringOrNull();
    }

    public static Setting deserializeFromJSON1(String s) throws org.json.JSONException {
        Serialization.JSONObjectWithNull o = new Serialization.JSONObjectWithNull(s);
        String key = Serialization.string_deserialize(o.getString("key"));
        String settingsKey = Serialization.string_deserialize(o.getString("settingsKey"));
        int chosenOptionPosition = Serialization.int_deserialize(o.getString("chosenOptionPosition"));
        String chosenOptionName = Serialization.string_deserialize(o.getString("chosenOptionName"));

        // This is a dummy object that only has to hold onto the data.
        Setting setting = UnknownSetting.createUnknownSetting(key, settingsKey);

        // The option may not actually be at this position anymore, but this was true at the time of serialization.
        setting.chosenOptionPosition = chosenOptionPosition;
        setting.chosenOptionName = chosenOptionName;

        return setting;
    }
}
