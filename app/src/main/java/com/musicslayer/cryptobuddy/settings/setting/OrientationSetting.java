package com.musicslayer.cryptobuddy.settings.setting;

import android.content.pm.ActivityInfo;

import java.util.ArrayList;

public class OrientationSetting extends Setting {
    public static Integer value;
    public void updateValue() { value = (Integer)getSettingValue(); }

    public String getKey() { return "OrientationSetting"; }
    public String getName() { return "OrientationSetting"; }
    public String getDisplayName() { return "Orientation"; }
    public String getSettingsKey() { return "orientation"; }

    public boolean needsRecreate() { return true; }

    public ArrayList<String> getOptionNames() {
        ArrayList<String> optionNames = new ArrayList<>();
        optionNames.add("Match System");
        optionNames.add("Landscape");
        optionNames.add("Portrait");
        return optionNames;
    }

    public String getDefaultOptionName() {
        return "Match System";
    }

    public ArrayList<String> getOptionDisplays() {
        ArrayList<String> optionDisplays = new ArrayList<>();
        optionDisplays.add("Match system setting for orientation.");
        optionDisplays.add("Always use landscape for this app.");
        optionDisplays.add("Always use portrait for this app.");
        return optionDisplays;
    }

    @SuppressWarnings("unchecked")
    public <T> ArrayList<T> getOptionValues() {
        ArrayList<Integer> optionValues = new ArrayList<>();
        optionValues.add(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        optionValues.add(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        optionValues.add(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        return (ArrayList<T>)optionValues;
    }
}
