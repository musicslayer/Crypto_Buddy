package com.musicslayer.cryptobuddy.settings;

import java.util.ArrayList;

public class ConfirmationSetting extends Setting {
    public static Boolean value;
    public void updateValue() { value = (Boolean)getOptionValues().get(chosenOptionPosition); }

    public String getKey() { return "ConfirmationSetting"; }
    public String getName() { return "ConfirmationSetting"; }
    public String getDisplayName() { return "Confirmation"; }
    public String getSettingsKey() { return "confirm"; }

    public ArrayList<String> getOptionNames() {
        ArrayList<String> optionNames = new ArrayList<>();
        optionNames.add("Use Confirmations");
        optionNames.add("Do Not Use Confirmations");
        return optionNames;
    }

    public ArrayList<String> getOptionDisplays() {
        ArrayList<String> optionDisplays = new ArrayList<>();
        optionDisplays.add("Confirm deletes and resets with a small input code to prevent accidental removals.");
        optionDisplays.add("Do not use any additional confirmations for deletes and resets.");
        return optionDisplays;
    }

    @SuppressWarnings("unchecked")
    public <T> ArrayList<T> getOptionValues() {
        ArrayList<Boolean> optionValues = new ArrayList<>();
        optionValues.add(true);
        optionValues.add(false);
        return (ArrayList<T>)optionValues;
    }
}
