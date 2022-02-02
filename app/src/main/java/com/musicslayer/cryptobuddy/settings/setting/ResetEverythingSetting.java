package com.musicslayer.cryptobuddy.settings.setting;

import android.content.Context;

import com.musicslayer.cryptobuddy.view.settings.ResetEverythingSettingsView;
import com.musicslayer.cryptobuddy.view.settings.SettingsView;

import java.util.ArrayList;

public class ResetEverythingSetting extends Setting {
    public static String value;
    public void updateValue() { value = (String)getSettingValue(); }

    public String getKey() { return "ResetEverythingSetting"; }
    public String getName() { return "ResetEverythingSetting"; }
    public String getDisplayName() { return "Reset Everything"; }
    public String getSettingsKey() { return "reset_everything"; }

    public ArrayList<String> getOptionNames() {
        ArrayList<String> optionNames = new ArrayList<>();
        optionNames.add("?");
        return optionNames;
    }

    public String getDefaultOptionName() {
        return "?";
    }

    public ArrayList<String> getOptionDisplays() {
        ArrayList<String> optionDisplays = new ArrayList<>();
        optionDisplays.add("?");
        return optionDisplays;
    }

    @SuppressWarnings("unchecked")
    public <T> ArrayList<T> getOptionValues() {
        ArrayList<String> optionValues = new ArrayList<>();
        optionValues.add("?");
        return (ArrayList<T>)optionValues;
    }

    public SettingsView createSettingView(Context context) {
        return new ResetEverythingSettingsView(context, this);
    }
}
