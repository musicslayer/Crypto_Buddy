package com.musicslayer.cryptobuddy.settings.setting;

import android.content.Context;

import com.musicslayer.cryptobuddy.view.settings.DeleteDownloadedTokensSettingsView;
import com.musicslayer.cryptobuddy.view.settings.SettingsView;

import java.util.ArrayList;

public class DeleteDownloadedTokensSetting extends Setting {
    public void updateValue() {}

    public String getKey() { return "DeleteDownloadedTokensSetting"; }
    public String getName() { return "DeleteDownloadedTokensSetting"; }
    public String getDisplayName() { return "Delete Downloaded Tokens"; }
    public String getSettingsKey() { return "delete_downloaded_tokens"; }

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
        return new DeleteDownloadedTokensSettingsView(context, this);
    }
}
