package com.musicslayer.cryptobuddy.settings.setting;

import android.content.Context;

import com.musicslayer.cryptobuddy.view.settings.DeleteFoundTokensSettingsView;
import com.musicslayer.cryptobuddy.view.settings.SettingsView;

import java.util.ArrayList;

public class DeleteFoundTokensSetting extends Setting {
    public void updateValue() {}

    public String getKey() { return "DeleteFoundTokensSetting"; }
    public String getName() { return "DeleteFoundTokensSetting"; }
    public String getDisplayName() { return "Delete Found Tokens"; }
    public String getSettingsKey() { return "delete_found_tokens"; }

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
        return new DeleteFoundTokensSettingsView(context, this);
    }
}
