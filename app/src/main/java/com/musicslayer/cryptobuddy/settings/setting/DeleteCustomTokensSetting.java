package com.musicslayer.cryptobuddy.settings.setting;

import android.content.Context;

import com.musicslayer.cryptobuddy.view.settings.DeleteCustomTokensSettingsView;
import com.musicslayer.cryptobuddy.view.settings.SettingsView;

import java.util.ArrayList;

public class DeleteCustomTokensSetting extends Setting {
    public void updateValue() {}

    public String getKey() { return "DeleteCustomTokensSetting"; }
    public String getName() { return "DeleteCustomTokensSetting"; }
    public String getDisplayName() { return "Delete Custom Tokens"; }
    public String getSettingsKey() { return "delete_custom_tokens"; }

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
        return new DeleteCustomTokensSettingsView(context, this);
    }
}
