package com.musicslayer.cryptobuddy.settings.category;

import com.musicslayer.cryptobuddy.settings.setting.Setting;

import java.util.ArrayList;

public class DefaultSettingsCategory extends SettingsCategory {
    public String getName() { return "DefaultSettingsCategory"; }
    public String getDisplayName() { return "Default"; }

    public ArrayList<Setting> getSettings() {
        ArrayList<Setting> settingArrayList = new ArrayList<>();
        settingArrayList.add(Setting.getSettingFromKey("DefaultFiatSetting"));
        settingArrayList.add(Setting.getSettingFromKey("DefaultCoinSetting"));
        return settingArrayList;
    }
}
