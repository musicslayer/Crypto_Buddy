package com.musicslayer.cryptobuddy.settings.setting;

import android.content.Context;

import com.musicslayer.cryptobuddy.view.settings.DeleteCustomTokensSettingsView;
import com.musicslayer.cryptobuddy.view.settings.SettingsView;

import java.util.ArrayList;

public class DeleteCustomTokensSetting extends Setting {
    public static String value;
    public void updateValue() { value = (String)getOptionValues().get(chosenOptionPosition); }

    public String getKey() { return "DeleteCustomTokensSetting"; }
    public String getName() { return "DeleteCustomTokensSetting"; }
    public String getDisplayName() { return "Delete Custom Tokens"; }
    public String getSettingsKey() { return "delete_custom_tokens"; }

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
        return new DeleteCustomTokensSettingsView(context, this);
    }
}
