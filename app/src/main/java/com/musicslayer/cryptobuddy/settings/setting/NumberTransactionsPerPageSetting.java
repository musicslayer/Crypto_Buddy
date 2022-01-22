package com.musicslayer.cryptobuddy.settings.setting;

import java.util.ArrayList;

public class NumberTransactionsPerPageSetting extends Setting {
    public static Integer value;
    public void updateValue() { value = (Integer)getOptionValues().get(chosenOptionPosition); }

    public String getKey() { return "NumberTransactionsPerPageSetting"; }
    public String getName() { return "NumberTransactionsPerPageSetting"; }
    public String getDisplayName() { return "Number of Transactions Per Page"; }
    public String getSettingsKey() { return "number_transactions_per_page"; }

    public ArrayList<String> getOptionNames() {
        ArrayList<String> optionNames = new ArrayList<>();
        optionNames.add("10");
        optionNames.add("25");
        optionNames.add("50");
        optionNames.add("100");
        optionNames.add("200");
        return optionNames;
    }

    public ArrayList<String> getOptionDisplays() {
        ArrayList<String> optionDisplays = new ArrayList<>();
        optionDisplays.add("Show 10 transactions per page.\n** Showing too many transactions per page may be slower on older devices. **");
        optionDisplays.add("Show 25 transactions per page.\n** Showing too many transactions per page may be slower on older devices. **");
        optionDisplays.add("Show 50 transactions per page.\n** Showing too many transactions per page may be slower on older devices. **");
        optionDisplays.add("Show 100 transactions per page.\n** Showing too many transactions per page may be slower on older devices. **");
        optionDisplays.add("Show 200 transactions per page.\n** Showing too many transactions per page may be slower on older devices. **");
        return optionDisplays;
    }

    @SuppressWarnings("unchecked")
    public <T> ArrayList<T> getOptionValues() {
        ArrayList<Integer> optionValues = new ArrayList<>();
        optionValues.add(10);
        optionValues.add(25);
        optionValues.add(50);
        optionValues.add(100);
        optionValues.add(200);
        return (ArrayList<T>)optionValues;
    }
}
