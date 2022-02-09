package com.musicslayer.cryptobuddy.settings.setting;

import androidx.appcompat.app.AppCompatDelegate;

import java.util.ArrayList;

public class DarkModeSetting extends Setting {
    public static Integer value;
    public void updateValue() { value = getSettingValue(); }

    public String getKey() { return "DarkModeSetting"; }
    public String getName() { return "DarkModeSetting"; }
    public String getDisplayName() { return "Dark Mode"; }
    public String getSettingsKey() { return "dark"; }

    public boolean needsRecreate() { return true; }

    public ArrayList<String> getOptionNames() {
        ArrayList<String> optionNames = new ArrayList<>();
        optionNames.add("Match System");
        optionNames.add("On");
        optionNames.add("Off");
        return optionNames;
    }

    public String getDefaultOptionName() {
        return "Match System";
    }

    public ArrayList<String> getOptionDisplays() {
        ArrayList<String> optionDisplays = new ArrayList<>();
        optionDisplays.add("Match system setting for dark mode.");
        optionDisplays.add("Always use dark mode for this app.");
        optionDisplays.add("Never use dark mode for this app.");
        return optionDisplays;
    }

    @SuppressWarnings("unchecked")
    public <T> ArrayList<T> getOptionValues() {
        ArrayList<Integer> optionValues = new ArrayList<>();
        optionValues.add(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        optionValues.add(AppCompatDelegate.MODE_NIGHT_YES);
        optionValues.add(AppCompatDelegate.MODE_NIGHT_NO);
        return (ArrayList<T>)optionValues;
    }
}
