package com.musicslayer.cryptobuddy.settings.setting;

import android.content.Context;

import com.musicslayer.cryptobuddy.view.settings.ResetAllSettingsSettingsView;
import com.musicslayer.cryptobuddy.view.settings.SettingsView;

import java.util.ArrayList;

public class ResetAllSettingsSetting extends Setting {
    public void updateValue() {}

    public String getKey() { return "ResetAllSettingsSetting"; }
    public String getName() { return "ResetAllSettingsSetting"; }
    public String getDisplayName() { return "Reset All Settings"; }
    public String getSettingsKey() { return "reset_all_settings"; }

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
        return new ResetAllSettingsSettingsView(context, this);
    }
}
