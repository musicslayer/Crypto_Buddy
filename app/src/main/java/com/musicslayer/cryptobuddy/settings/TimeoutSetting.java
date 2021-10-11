package com.musicslayer.cryptobuddy.settings;

import java.util.ArrayList;

public class TimeoutSetting extends Setting {
    public static Long value;
    public void updateValue() { value = (Long)getOptionValues().get(chosenOptionPosition); }

    public String getKey() { return "TimeoutSetting"; }
    public String getName() { return "TimeoutSetting"; }
    public String getDisplayName() { return "Timeout"; }
    public String getSettingsKey() { return "timeout"; }

    public ArrayList<String> getOptionNames() {
        ArrayList<String> optionNames = new ArrayList<>();
        optionNames.add("10 Seconds");
        optionNames.add("30 Seconds");
        optionNames.add("60 Seconds");
        return optionNames;
    }

    public ArrayList<String> getOptionDisplays() {
        ArrayList<String> optionDisplays = new ArrayList<>();
        optionDisplays.add("10 seconds timeout for each API call.");
        optionDisplays.add("30 seconds timeout for each API call.");
        optionDisplays.add("60 seconds timeout for each API call.");
        return optionDisplays;
    }

    @SuppressWarnings("unchecked")
    public <T> ArrayList<T> getOptionValues() {
        ArrayList<Long> optionValues = new ArrayList<>();
        optionValues.add(10000L);
        optionValues.add(30000L);
        optionValues.add(60000L);
        return (ArrayList<T>)optionValues;
    }
}
