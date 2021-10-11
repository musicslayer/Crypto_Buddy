package com.musicslayer.cryptobuddy.settings;

import java.util.ArrayList;

public class LossValuesSetting extends Setting {
    public String getKey() { return "LossValuesSetting"; }
    public String getName() { return "LossValuesSetting"; }
    public String getDisplayName() { return "Loss Values"; }
    public String getSettingsKey() { return "loss"; }

    public ArrayList<String> getOptionNames() {
        ArrayList<String> optionNames = new ArrayList<>();
        optionNames.add("Match Locale");
        optionNames.add("Negative");
        optionNames.add("Red");
        optionNames.add("Parentheses");
        optionNames.add("Red Match Locale");
        optionNames.add("Red Negative");
        optionNames.add("Red Parentheses");
        return optionNames;
    }

    public ArrayList<String> getOptionDisplays() {
        ArrayList<String> optionDisplays = new ArrayList<>();
        optionDisplays.add("Show lost assets using numbers of the chosen locale's negative format.");
        optionDisplays.add("Show lost assets using negative numbers.");
        optionDisplays.add("Show lost assets using red positive numbers.");
        optionDisplays.add("Show lost assets using positive numbers in parentheses.");
        optionDisplays.add("Show lost assets using red numbers of the chosen locale's negative format.");
        optionDisplays.add("Show lost assets using red negative numbers.");
        optionDisplays.add("Show lost assets using red positive numbers in parentheses.");
        return optionDisplays;
    }

    @SuppressWarnings("unchecked")
    public <T> ArrayList<T> getOptionValues() {
        ArrayList<String> optionValues = new ArrayList<>();
        optionValues.add("match_locale");
        optionValues.add("negative");
        optionValues.add("red");
        optionValues.add("parentheses");
        optionValues.add("red_match_locale");
        optionValues.add("red_negative");
        optionValues.add("red_parentheses");
        return (ArrayList<T>)optionValues;
    }
}
