package com.musicslayer.cryptobuddy.settings.setting;

import android.content.Context;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.data.bridge.DataBridge;
import com.musicslayer.cryptobuddy.util.FileUtil;
import com.musicslayer.cryptobuddy.util.ReflectUtil;
import com.musicslayer.cryptobuddy.view.settings.SettingsView;
import com.musicslayer.cryptobuddy.view.settings.StandardSettingsView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

abstract public class Setting implements DataBridge.SerializableToJSON {
    public static ArrayList<Setting> settings;
    public static HashMap<String, Setting> setting_map;
    public static HashMap<String, Setting> setting_settings_map;
    public static ArrayList<String> setting_names;
    public static ArrayList<String> setting_display_names;

    public String chosenOptionName;
    public String chosenOptionDisplay;

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

    public int getChosenOptionNameIndex() {
        // This is used by the SettingsActivity's spinners.
        // Return the index that goes with the chosen option name.
        return this.getOptionNames().indexOf(chosenOptionName);
    }

    public void setSetting(int chosenOptionNameIdx) {
        // This is used by the SettingsActivity's spinners.
        setSetting(this.getOptionNames().get(chosenOptionNameIdx));
    }

    public void setSetting(String chosenOptionName) {
        this.chosenOptionName = chosenOptionName;
        this.chosenOptionDisplay = getOptionDisplays().get(this.getOptionNames().indexOf(chosenOptionName));

        // Each setting must call this method because the value may be of a different type.
        updateValue();
    }

    @SuppressWarnings("unchecked")
    public <T> T getSettingValue() {
        int idx = getOptionNames().indexOf(chosenOptionName);
        return (T)getOptionValues().get(idx);
    }

    // Can be used to modify how we display options with extra data that we don't want to store (like the time zone offset).
    // This does not affect the option name itself, merely how it is displayed.
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

    public static void initialize() {
        settings = new ArrayList<>();
        setting_map = new HashMap<>();
        setting_settings_map = new HashMap<>();
        setting_display_names = new ArrayList<>();

        setting_names = FileUtil.readFileIntoLines(R.raw.settings_setting);
        for(String settingName : setting_names) {
            Setting setting = ReflectUtil.constructClassInstanceFromName("com.musicslayer.cryptobuddy.settings.setting." + settingName);

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

    public static Setting getSettingFromSettingsKey(String settingsKey) {
        Setting setting = setting_settings_map.get(settingsKey);
        if(setting == null) {
            setting = UnknownSetting.createUnknownSetting("?", settingsKey);
        }

        return setting;
    }

    public void refreshSetting() {
        // If the setting's current choice no longer exists, set it to the default value.
        // Otherwise, do nothing.
        if(!this.getOptionNames().contains(this.chosenOptionName)) {
            resetSetting();
        }
    }

    public void resetSetting() {
        // Resets a setting to its default value.
        this.setSetting(this.getDefaultOptionName());
    }

    @Override
    public void serializeToJSON(DataBridge.Writer o) throws IOException {
        o.beginObject()
                .serialize("key", getKey(), String.class)
                .serialize("settingsKey", getSettingsKey(), String.class)
                .serialize("chosenOptionName", chosenOptionName, String.class)
                .endObject();
    }

    public static Setting deserializeFromJSON(DataBridge.Reader o) throws IOException {
        o.beginObject();
        String key = o.deserialize("key", String.class);
        String settingsKey = o.deserialize("settingsKey", String.class);
        String chosenOptionName = o.deserialize("chosenOptionName", String.class);
        o.endObject();

        // This is a dummy object that only has to hold onto the data.
        Setting setting = UnknownSetting.createUnknownSetting(key, settingsKey);

        // The option may not actually exist anymore, but it did at the time of serialization.
        setting.chosenOptionName = chosenOptionName;

        return setting;
    }
}
