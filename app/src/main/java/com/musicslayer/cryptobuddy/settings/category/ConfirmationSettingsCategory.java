package com.musicslayer.cryptobuddy.settings.category;

import com.musicslayer.cryptobuddy.settings.setting.Setting;

import java.util.ArrayList;

public class ConfirmationSettingsCategory extends SettingsCategory {
    public String getName() { return "ConfirmationSettingsCategory"; }
    public String getDisplayName() { return "Confirmation"; }

    public ArrayList<Setting> getSettings() {
        ArrayList<Setting> settingArrayList = new ArrayList<>();
        settingArrayList.add(Setting.getSettingFromKey("ConfirmationSetting"));
        return settingArrayList;
    }
}
