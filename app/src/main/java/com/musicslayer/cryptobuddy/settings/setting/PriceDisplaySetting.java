package com.musicslayer.cryptobuddy.settings.setting;

import java.util.ArrayList;

public class PriceDisplaySetting extends Setting {
    public static String value;
    public void updateValue() { value = (String)getSettingValue(); }

    public String getKey() { return "PriceDisplaySetting"; }
    public String getName() { return "PriceDisplaySetting"; }
    public String getDisplayName() { return "Price Display"; }
    public String getSettingsKey() { return "price"; }

    public ArrayList<String> getOptionNames() {
        ArrayList<String> optionNames = new ArrayList<>();
        optionNames.add("Forward and Backward");
        optionNames.add("Forward");
        optionNames.add("None");
        return optionNames;
    }

    public String getDefaultOptionName() {
        return "Forward";
    }

    public ArrayList<String> getOptionDisplays() {
        ArrayList<String> optionDisplays = new ArrayList<>();
        optionDisplays.add("Show price in both forward and backward directions. For example, if you buy or sell 1 BTC for 20000 USD, the forward price is 1 BTC / 20000 USD, and the backward price is 1 USD / 0.00005 BTC.");
        optionDisplays.add("Show price in forward direction. For example, if you buy or sell 1 BTC for 20000 USD, the forward price is 1 BTC / 20000 USD.");
        optionDisplays.add("Do not show price in tables.\n(Forward price is still shown in Price and Converter tools.)");
        return optionDisplays;
    }

    @SuppressWarnings("unchecked")
    public <T> ArrayList<T> getOptionValues() {
        ArrayList<String> optionValues = new ArrayList<>();
        optionValues.add("ForwardBackward");
        optionValues.add("Forward");
        optionValues.add("None");
        return (ArrayList<T>)optionValues;
    }
}
