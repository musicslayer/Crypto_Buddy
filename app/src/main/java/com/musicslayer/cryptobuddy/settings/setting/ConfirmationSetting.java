package com.musicslayer.cryptobuddy.settings.setting;

import java.util.ArrayList;

public class ConfirmationSetting extends Setting {
    public static String value;
    public void updateValue() { value = (String)getOptionValues().get(chosenOptionPosition); }

    public String getKey() { return "ConfirmationSetting"; }
    public String getName() { return "ConfirmationSetting"; }
    public String getDisplayName() { return "Confirmation"; }
    public String getSettingsKey() { return "confirm"; }

    public ArrayList<String> getOptionNames() {
        ArrayList<String> optionNames = new ArrayList<>();
        optionNames.add("Use Confirmation Code");
        optionNames.add("Use Confirmation Dialog");
        optionNames.add("Do Not Use Confirmations");
        return optionNames;
    }

    public ArrayList<String> getOptionDisplays() {
        ArrayList<String> optionDisplays = new ArrayList<>();
        optionDisplays.add("Confirm deletes and resets with a small input code to help prevent accidental removals.");
        optionDisplays.add("Confirm deletes and resets with a dialog to help prevent accidental removals.");
        optionDisplays.add("Do not use any additional confirmations for deletes and resets.\n** Not Recommended **");
        return optionDisplays;
    }

    @SuppressWarnings("unchecked")
    public <T> ArrayList<T> getOptionValues() {
        ArrayList<String> optionValues = new ArrayList<>();
        optionValues.add("Code");
        optionValues.add("Dialog");
        optionValues.add("None");
        return (ArrayList<T>)optionValues;
    }
}
