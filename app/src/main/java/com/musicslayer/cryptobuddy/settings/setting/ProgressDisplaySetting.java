package com.musicslayer.cryptobuddy.settings.setting;

import java.util.ArrayList;

public class ProgressDisplaySetting extends Setting {
    public static String value;
    public void updateValue() { value = (String)getSettingValue(); }

    public String getKey() { return "ProgressDisplaySetting"; }
    public String getName() { return "ProgressDisplaySetting"; }
    public String getDisplayName() { return "Progress Display"; }
    public String getSettingsKey() { return "progress_display"; }

    public ArrayList<String> getOptionNames() {
        ArrayList<String> optionNames = new ArrayList<>();
        optionNames.add("Combo");
        optionNames.add("Percentage");
        optionNames.add("Total");
        optionNames.add("None");
        return optionNames;
    }

    public String getDefaultOptionName() {
        return "Combo";
    }

    public ArrayList<String> getOptionDisplays() {
        ArrayList<String> optionDisplays = new ArrayList<>();
        optionDisplays.add("Show progress as both a number complete out of the total and a percentage complete.\n7/20 (35%) Finished");
        optionDisplays.add("Show progress as a percentage complete.\n35% Finished");
        optionDisplays.add("Show progress as a number complete out of the total.\n7/20 Finished");
        optionDisplays.add("Do not show progress.\n** Not Recommended **");
        return optionDisplays;
    }

    @SuppressWarnings("unchecked")
    public <T> ArrayList<T> getOptionValues() {
        ArrayList<String> optionValues = new ArrayList<>();
        optionValues.add("combo");
        optionValues.add("percentage");
        optionValues.add("total");
        optionValues.add("none");
        return (ArrayList<T>)optionValues;
    }
}
