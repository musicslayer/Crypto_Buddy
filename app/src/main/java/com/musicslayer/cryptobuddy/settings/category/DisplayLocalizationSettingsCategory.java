package com.musicslayer.cryptobuddy.settings.category;

import com.musicslayer.cryptobuddy.settings.setting.Setting;

import java.util.ArrayList;

public class DisplayLocalizationSettingsCategory extends SettingsCategory {
    public String getName() { return "DisplayLocalizationSettingsCategory"; }
    public String getDisplayName() { return "Display Localization"; }

    public ArrayList<Setting> getSettings() {
        ArrayList<Setting> settingArrayList = new ArrayList<>();
        settingArrayList.add(Setting.getSettingFromKey("NumericLocaleSetting"));
        settingArrayList.add(Setting.getSettingFromKey("DatetimeLocaleSetting"));
        settingArrayList.add(Setting.getSettingFromKey("TimeZoneSetting"));
        return settingArrayList;
    }
}
