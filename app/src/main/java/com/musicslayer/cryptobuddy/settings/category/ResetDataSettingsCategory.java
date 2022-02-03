package com.musicslayer.cryptobuddy.settings.category;

import com.musicslayer.cryptobuddy.persistence.Purchases;
import com.musicslayer.cryptobuddy.settings.setting.Setting;

import java.util.ArrayList;

public class ResetDataSettingsCategory extends SettingsCategory {
    public String getName() { return "ResetDataSettingsCategory"; }
    public String getDisplayName() { return "Reset Data"; }

    public ArrayList<Setting> getSettings() {
        ArrayList<Setting> settingArrayList = new ArrayList<>();
        settingArrayList.add(Setting.getSettingFromKey("ResetAllSettingsSetting"));
        settingArrayList.add(Setting.getSettingFromKey("DeleteAllAddressHistorySetting"));
        settingArrayList.add(Setting.getSettingFromKey("DeleteAllTransactionPortfoliosSetting"));
        settingArrayList.add(Setting.getSettingFromKey("DeleteAllAddressPortfoliosSetting"));
        settingArrayList.add(Setting.getSettingFromKey("DeleteAllExchangePortfoliosSetting"));
        settingArrayList.add(Setting.getSettingFromKey("DeleteFiatsSetting"));
        settingArrayList.add(Setting.getSettingFromKey("DeleteCoinsSetting"));

        if(Purchases.isUnlockTokensPurchased()) {
            settingArrayList.add(Setting.getSettingFromKey("DeleteTokensSetting"));
        }

        // This should be presented last, as it resets everything.
        settingArrayList.add(Setting.getSettingFromKey("ResetEverythingSetting"));
        return settingArrayList;
    }
}
