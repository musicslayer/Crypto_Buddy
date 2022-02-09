package com.musicslayer.cryptobuddy.settings.setting;

import android.content.Context;

import com.musicslayer.cryptobuddy.view.settings.DeleteAllAddressHistorySettingsView;
import com.musicslayer.cryptobuddy.view.settings.SettingsView;

import java.util.ArrayList;

public class DeleteAllAddressHistorySetting extends Setting {
    public static String value;
    public void updateValue() { value = getSettingValue(); }

    public String getKey() { return "DeleteAllAddressHistorySetting"; }
    public String getName() { return "DeleteAllAddressHistorySetting"; }
    public String getDisplayName() { return "Delete All Address History"; }
    public String getSettingsKey() { return "delete_all_address_history"; }

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
        return new DeleteAllAddressHistorySettingsView(context, this);
    }
}
