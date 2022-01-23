package com.musicslayer.cryptobuddy.settings.category;

import com.musicslayer.cryptobuddy.settings.setting.Setting;

import java.util.ArrayList;

public class AppearanceSettingsCategory extends SettingsCategory {
    public String getName() { return "AppearanceSettingsCategory"; }
    public String getDisplayName() { return "Appearance"; }

    public ArrayList<Setting> getSettings() {
        ArrayList<Setting> settingArrayList = new ArrayList<>();
        settingArrayList.add(Setting.getSettingFromKey("MessageLengthSetting"));
        settingArrayList.add(Setting.getSettingFromKey("DarkModeSetting"));
        settingArrayList.add(Setting.getSettingFromKey("OrientationSetting"));
        return settingArrayList;
    }
}
