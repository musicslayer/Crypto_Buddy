package com.musicslayer.cryptobuddy.settings.setting;

import java.util.ArrayList;

public class NetworksSetting extends Setting {
    public static String value;
    public void updateValue() { value = (String)getOptionValues().get(chosenOptionPosition); }

    public String getKey() { return "NetworksSetting"; }
    public String getName() { return "NetworksSetting"; }
    public String getDisplayName() { return "Networks"; }
    public String getSettingsKey() { return "network"; }

    public ArrayList<String> getOptionNames() {
        ArrayList<String> optionNames = new ArrayList<>();
        optionNames.add("All");
        optionNames.add("Mainnet");
        return optionNames;
    }

    public ArrayList<String> getOptionDisplays() {
        ArrayList<String> optionDisplays = new ArrayList<>();
        optionDisplays.add("Recognize all addresses.");
        optionDisplays.add("Only recognize Mainnet addresses.");
        return optionDisplays;
    }

    @SuppressWarnings("unchecked")
    public <T> ArrayList<T> getOptionValues() {
        ArrayList<String> optionValues = new ArrayList<>();
        optionValues.add("All");
        optionValues.add("Mainnet");
        return (ArrayList<T>)optionValues;
    }
}
