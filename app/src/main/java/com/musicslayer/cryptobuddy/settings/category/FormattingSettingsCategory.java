package com.musicslayer.cryptobuddy.settings.category;

import com.musicslayer.cryptobuddy.settings.setting.Setting;

import java.util.ArrayList;

public class FormattingSettingsCategory extends SettingsCategory {
    public String getName() { return "FormattingSettingsCategory"; }
    public String getDisplayName() { return "Formatting"; }

    public ArrayList<Setting> getSettings() {
        ArrayList<Setting> settingArrayList = new ArrayList<>();
        settingArrayList.add(Setting.getSettingFromKey("DatetimeFormatSetting"));
        settingArrayList.add(Setting.getSettingFromKey("NumberDecimalPlacesSetting"));
        settingArrayList.add(Setting.getSettingFromKey("PriceDisplaySetting"));
        settingArrayList.add(Setting.getSettingFromKey("LossValuesSetting"));
        settingArrayList.add(Setting.getSettingFromKey("AssetDisplaySetting"));
        return settingArrayList;
    }
}
