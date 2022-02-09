package com.musicslayer.cryptobuddy.settings.setting;

import java.util.ArrayList;

public class AssetDisplaySetting extends Setting {
    public static String value;
    public void updateValue() { value = getSettingValue(); }

    public String getKey() { return "AssetDisplaySetting"; }
    public String getName() { return "AssetDisplaySetting"; }
    public String getDisplayName() { return "Asset Display"; }
    public String getSettingsKey() { return "asset"; }

    public ArrayList<String> getOptionNames() {
        ArrayList<String> optionNames = new ArrayList<>();
        optionNames.add("Use Full Asset Names and Symbols");
        optionNames.add("Use Full Asset Names");
        optionNames.add("Use Asset Symbols");
        return optionNames;
    }

    public String getDefaultOptionName() {
        return "Use Full Asset Names and Symbols";
    }

    public ArrayList<String> getOptionDisplays() {
        ArrayList<String> optionDisplays = new ArrayList<>();
        optionDisplays.add("Use full asset names and symbols.\nUS Dollar (USD), Bitcoin (BTC), Litecoin (LTC), Dogecoin (DOGE)");
        optionDisplays.add("Use full asset names.\nUS Dollar, Bitcoin, Litecoin, Dogecoin");
        optionDisplays.add("Use asset symbols.\nUSD, BTC, LTC, DOGE");
        return optionDisplays;
    }

    @SuppressWarnings("unchecked")
    public <T> ArrayList<T> getOptionValues() {
        ArrayList<String> optionValues = new ArrayList<>();
        optionValues.add("combo");
        optionValues.add("full");
        optionValues.add("symbol");
        return (ArrayList<T>)optionValues;
    }
}
