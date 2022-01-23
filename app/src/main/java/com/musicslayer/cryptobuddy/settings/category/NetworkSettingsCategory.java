package com.musicslayer.cryptobuddy.settings.category;

import com.musicslayer.cryptobuddy.settings.setting.Setting;

import java.util.ArrayList;

public class NetworkSettingsCategory extends SettingsCategory {
    public String getName() { return "NetworkSettingsCategory"; }
    public String getDisplayName() { return "Network"; }

    public ArrayList<Setting> getSettings() {
        ArrayList<Setting> settingArrayList = new ArrayList<>();
        settingArrayList.add(Setting.getSettingFromKey("NetworksSetting"));
        settingArrayList.add(Setting.getSettingFromKey("TimeoutSetting"));
        settingArrayList.add(Setting.getSettingFromKey("MaxNumberTransactionsSetting"));
        settingArrayList.add(Setting.getSettingFromKey("NumberTransactionsPerPageSetting"));
        return settingArrayList;
    }
}
