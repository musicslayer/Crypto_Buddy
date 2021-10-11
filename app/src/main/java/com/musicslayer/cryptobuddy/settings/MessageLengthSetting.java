package com.musicslayer.cryptobuddy.settings;

import android.widget.Toast;

import java.util.ArrayList;

public class MessageLengthSetting extends Setting {
    public String getKey() { return "MessageLengthSetting"; }
    public String getName() { return "MessageLengthSetting"; }
    public String getDisplayName() { return "Message Length"; }
    public String getSettingsKey() { return "message"; }

    public ArrayList<String> getOptionNames() {
        ArrayList<String> optionNames = new ArrayList<>();
        optionNames.add("Short");
        optionNames.add("Long");
        return optionNames;
    }

    public ArrayList<String> getOptionDisplays() {
        ArrayList<String> optionDisplays = new ArrayList<>();
        optionDisplays.add("Show messages for a shorter time.");
        optionDisplays.add("Show messages for a longer time.");
        return optionDisplays;
    }

    @SuppressWarnings("unchecked")
    public <T> ArrayList<T> getOptionValues() {
        ArrayList<Integer> optionValues = new ArrayList<>();
        optionValues.add(Toast.LENGTH_SHORT);
        optionValues.add(Toast.LENGTH_LONG);
        return (ArrayList<T>)optionValues;
    }
}
