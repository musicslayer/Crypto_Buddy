package com.musicslayer.cryptobuddy.settings.category;

import com.musicslayer.cryptobuddy.settings.setting.Setting;

import java.util.ArrayList;

public class ProgressSettingsCategory extends SettingsCategory {
    public String getName() { return "ProgressSettingsCategory"; }
    public String getDisplayName() { return "Progress"; }

    public ArrayList<Setting> getSettings() {
        ArrayList<Setting> settingArrayList = new ArrayList<>();
        settingArrayList.add(Setting.getSettingFromKey("ProgressDisplaySetting"));
        return settingArrayList;
    }
}
