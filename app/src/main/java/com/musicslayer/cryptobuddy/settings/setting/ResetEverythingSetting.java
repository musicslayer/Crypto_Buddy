package com.musicslayer.cryptobuddy.settings.setting;

import android.content.Context;

import com.musicslayer.cryptobuddy.view.settings.ResetEverythingSettingsView;
import com.musicslayer.cryptobuddy.view.settings.SettingsView;

import java.util.ArrayList;

public class ResetEverythingSetting extends Setting {
    public void updateValue() {}

    public String getKey() { return "ResetEverythingSetting"; }
    public String getName() { return "ResetEverythingSetting"; }
    public String getDisplayName() { return "Reset Everything"; }
    public String getSettingsKey() { return "reset_everything"; }

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
        return new ResetEverythingSettingsView(context, this);
    }
}
