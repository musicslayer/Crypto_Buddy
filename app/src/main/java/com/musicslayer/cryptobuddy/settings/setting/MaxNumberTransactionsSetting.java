package com.musicslayer.cryptobuddy.settings.setting;

import java.util.ArrayList;

public class MaxNumberTransactionsSetting extends Setting {
    public static Integer value;
    public void updateValue() { value = (Integer)getSettingValue(); }

    public String getKey() { return "MaxNumberTransactionsSetting"; }
    public String getName() { return "MaxNumberTransactionsSetting"; }
    public String getDisplayName() { return "Max Number of Transactions"; }
    public String getSettingsKey() { return "max_transactions"; }

    public ArrayList<String> getOptionNames() {
        ArrayList<String> optionNames = new ArrayList<>();
        optionNames.add("500");
        optionNames.add("1000");
        optionNames.add("5000");
        optionNames.add("10000");
        optionNames.add("50000");
        return optionNames;
    }

    public String getDefaultOptionName() {
        return "5000";
    }

    public ArrayList<String> getOptionDisplays() {
        ArrayList<String> optionDisplays = new ArrayList<>();
        optionDisplays.add("Analyze up to 500 transactions per address.\n** Excessive transactions may crash older devices. **");
        optionDisplays.add("Analyze up to 1000 transactions per address.\n** Excessive transactions may crash older devices. **");
        optionDisplays.add("Analyze up to 5000 transactions per address.\n** Excessive transactions may crash older devices. **");
        optionDisplays.add("Analyze up to 10000 transactions per address.\n** Excessive transactions may crash older devices. **");
        optionDisplays.add("Analyze up to 50000 transactions per address.\n** Excessive transactions may crash older devices. **");
        return optionDisplays;
    }

    @SuppressWarnings("unchecked")
    public <T> ArrayList<T> getOptionValues() {
        ArrayList<Integer> optionValues = new ArrayList<>();
        optionValues.add(500);
        optionValues.add(1000);
        optionValues.add(5000);
        optionValues.add(10000);
        optionValues.add(50000);
        return (ArrayList<T>)optionValues;
    }
}
