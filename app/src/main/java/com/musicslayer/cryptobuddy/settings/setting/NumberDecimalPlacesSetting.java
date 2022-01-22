package com.musicslayer.cryptobuddy.settings.setting;

import java.util.ArrayList;

public class NumberDecimalPlacesSetting extends Setting {
    public static String value;
    public void updateValue() { value = (String)getOptionValues().get(chosenOptionPosition); }

    public String getKey() { return "NumberDecimalPlacesSetting"; }
    public String getName() { return "NumberDecimalPlacesSetting"; }
    public String getDisplayName() { return "Number of Decimal Places"; }
    public String getSettingsKey() { return "decimal"; }

    public ArrayList<String> getOptionNames() {
        ArrayList<String> optionNames = new ArrayList<>();
        optionNames.add("Fixed");
        optionNames.add("Truncated");
        return optionNames;
    }

    public ArrayList<String> getOptionDisplays() {
        ArrayList<String> optionDisplays = new ArrayList<>();
        optionDisplays.add("Show all decimal places expected for the crypto.\n0.00010000 BTC.");
        optionDisplays.add("Start with all decimal places expected for the crypto, and then truncate trailing zeroes.\n0.0001 BTC.");
        return optionDisplays;
    }

    @SuppressWarnings("unchecked")
    public <T> ArrayList<T> getOptionValues() {
        ArrayList<String> optionValues = new ArrayList<>();
        optionValues.add("Fixed");
        optionValues.add("Truncated");
        return (ArrayList<T>)optionValues;
    }
}
