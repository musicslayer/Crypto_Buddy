package com.musicslayer.cryptobuddy.settings.setting;

import java.util.ArrayList;

public class TimeoutSetting extends Setting {
    public static Long value;
    public void updateValue() { value = (Long)getSettingValue(); }

    public String getKey() { return "TimeoutSetting"; }
    public String getName() { return "TimeoutSetting"; }
    public String getDisplayName() { return "Timeout"; }
    public String getSettingsKey() { return "timeout"; }

    public ArrayList<String> getOptionNames() {
        ArrayList<String> optionNames = new ArrayList<>();
        optionNames.add("30 Seconds");
        optionNames.add("60 Seconds");
        optionNames.add("120 Seconds");
        return optionNames;
    }

    public String getDefaultOptionName() {
        return "60 Seconds";
    }

    public ArrayList<String> getOptionDisplays() {
        ArrayList<String> optionDisplays = new ArrayList<>();
        optionDisplays.add("30 seconds timeout for each API call.");
        optionDisplays.add("60 seconds timeout for each API call.");
        optionDisplays.add("120 seconds timeout for each API call.");
        return optionDisplays;
    }

    @SuppressWarnings("unchecked")
    public <T> ArrayList<T> getOptionValues() {
        ArrayList<Long> optionValues = new ArrayList<>();
        optionValues.add(30000L);
        optionValues.add(60000L);
        optionValues.add(120000L);
        return (ArrayList<T>)optionValues;
    }
}
