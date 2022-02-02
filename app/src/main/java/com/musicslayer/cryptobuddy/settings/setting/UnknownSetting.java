package com.musicslayer.cryptobuddy.settings.setting;

import java.util.ArrayList;

public class UnknownSetting extends Setting {
    public static String value;
    public void updateValue() { value = (String)getOptionValues().get(chosenOptionPosition); }

    String key;
    String settingsKey;

    public String getKey() { return key; }
    public String getName() { return "UnknownSetting"; }
    public String getDisplayName() { return "Unknown Setting"; }
    public String getSettingsKey() { return settingsKey; }

    public ArrayList<String> getOptionNames() {
        ArrayList<String> optionNames = new ArrayList<>();
        optionNames.add("?");
        return optionNames;
    }

    public String getDefaultOptionName() {
        return "?";
    }

    public ArrayList<String> getOptionDisplays() {
        ArrayList<String> optionDisplays = new ArrayList<>();
        optionDisplays.add("?");
        return optionDisplays;
    }

    @SuppressWarnings("unchecked")
    public <T> ArrayList<T> getOptionValues() {
        ArrayList<String> optionValues = new ArrayList<>();
        optionValues.add("?");
        return (ArrayList<T>)optionValues;
    }

    public static UnknownSetting createUnknownSetting(String key, String settingsKey) {
        return new UnknownSetting(key, settingsKey);
    }

    private UnknownSetting(String key, String settingsKey) {
        this.key = key;
        this.settingsKey = settingsKey;
    }
}
