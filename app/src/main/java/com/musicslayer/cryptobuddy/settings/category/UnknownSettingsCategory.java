package com.musicslayer.cryptobuddy.settings.category;

import com.musicslayer.cryptobuddy.settings.setting.Setting;

import java.util.ArrayList;

public class UnknownSettingsCategory extends SettingsCategory {
    String key;

    public String getKey() { return key; }
    public String getName() { return "UnknownSettingsCategory"; }
    public String getDisplayName() { return "Unknown Settings Category"; }

    public static UnknownSettingsCategory createUnknownSettingsCategory(String key) {
        return new UnknownSettingsCategory(key);
    }

    public UnknownSettingsCategory(String key) {
        this.key = key;
    }

    public ArrayList<Setting> getSettings() { return new ArrayList<>(); }
}
