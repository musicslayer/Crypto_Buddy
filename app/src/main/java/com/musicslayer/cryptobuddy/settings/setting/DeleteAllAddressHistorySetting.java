package com.musicslayer.cryptobuddy.settings.setting;

import android.content.Context;

import com.musicslayer.cryptobuddy.view.settings.DeleteAllAddressHistorySettingsView;
import com.musicslayer.cryptobuddy.view.settings.SettingsView;

import java.util.ArrayList;

public class DeleteAllAddressHistorySetting extends Setting {
    public void updateValue() {}

    public String getKey() { return "DeleteAllAddressHistorySetting"; }
    public String getName() { return "DeleteAllAddressHistorySetting"; }
    public String getDisplayName() { return "Delete All Address History"; }
    public String getSettingsKey() { return "delete_all_address_history"; }

    public void setSetting(int chosenOptionPosition) {}

    public ArrayList<String> getOptionNames() {
        return null;
    }

    public ArrayList<String> getOptionDisplays() {
        return null;
    }

    public <T> ArrayList<T> getOptionValues() {
        return null;
    }

    public SettingsView createSettingView(Context context) {
        return new DeleteAllAddressHistorySettingsView(context, this);
    }
}
