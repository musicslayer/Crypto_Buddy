package com.musicslayer.cryptobuddy.settings.category;

import android.content.Context;

import com.musicslayer.cryptobuddy.persistence.Purchases;
import com.musicslayer.cryptobuddy.settings.setting.Setting;

import java.util.ArrayList;

public class SettingCategory {
    public static ArrayList<SettingCategory> setting_categories;
    public static ArrayList<String> settings_category_display_names;

    public String displayName;
    public ArrayList<Setting> settingArrayList;

    public static void initialize(Context context) {
        // These are fully specified here instead of reading from a file.
        // The order of each category and the settings within a category matches what the user will see.
        setting_categories = new ArrayList<>();
        settings_category_display_names = new ArrayList<>();

        String c1 = "Formatting";
        ArrayList<Setting> s1 = new ArrayList<>();
        s1.add(Setting.getSettingFromKey("DatetimeFormatSetting"));
        s1.add(Setting.getSettingFromKey("NumberDecimalPlacesSetting"));
        s1.add(Setting.getSettingFromKey("PriceDisplaySetting"));
        s1.add(Setting.getSettingFromKey("LossValuesSetting"));
        s1.add(Setting.getSettingFromKey("AssetDisplaySetting"));
        setting_categories.add(new SettingCategory(c1, s1));
        settings_category_display_names.add(c1);

        String c2 = "Display Localization";
        ArrayList<Setting> s2 = new ArrayList<>();
        s2.add(Setting.getSettingFromKey("NumericLocaleSetting"));
        s2.add(Setting.getSettingFromKey("DatetimeLocaleSetting"));
        s2.add(Setting.getSettingFromKey("TimeZoneSetting"));
        setting_categories.add(new SettingCategory(c2, s2));
        settings_category_display_names.add(c2);

        String c3 = "API";
        ArrayList<Setting> s3 = new ArrayList<>();
        s3.add(Setting.getSettingFromKey("NetworksSetting"));
        s3.add(Setting.getSettingFromKey("TimeoutSetting"));
        s3.add(Setting.getSettingFromKey("MaxNumberTransactionsSetting"));
        s3.add(Setting.getSettingFromKey("NumberTransactionsPerPageSetting"));
        setting_categories.add(new SettingCategory(c3, s3));
        settings_category_display_names.add(c3);

        String c4 = "Appearance";
        ArrayList<Setting> s4 = new ArrayList<>();
        s4.add(Setting.getSettingFromKey("MessageLengthSetting"));
        s4.add(Setting.getSettingFromKey("DarkModeSetting"));
        s4.add(Setting.getSettingFromKey("OrientationSetting"));
        setting_categories.add(new SettingCategory(c4, s4));
        settings_category_display_names.add(c4);

        String c5 = "Confirmations";
        ArrayList<Setting> s5 = new ArrayList<>();
        s5.add(Setting.getSettingFromKey("NumericLocaleSetting"));
        setting_categories.add(new SettingCategory(c5, s5));
        settings_category_display_names.add(c5);

        String c6 = "Reset Data";
        ArrayList<Setting> s6 = new ArrayList<>();
        s6.add(Setting.getSettingFromKey("ResetAllSettingsSetting"));
        s6.add(Setting.getSettingFromKey("DeleteAllAddressHistorySetting"));
        s6.add(Setting.getSettingFromKey("DeleteAllTransactionPortfoliosSetting"));
        s6.add(Setting.getSettingFromKey("DeleteAllAddressPortfoliosSetting"));
        s6.add(Setting.getSettingFromKey("DeleteAllExchangePortfoliosSetting"));

        if(Purchases.isUnlockTokensPurchased()) {
            s6.add(Setting.getSettingFromKey("DeleteDownloadedTokensSetting"));
            s6.add(Setting.getSettingFromKey("DeleteFoundTokensSetting"));
            s6.add(Setting.getSettingFromKey("DeleteCustomTokensSetting"));
        }

        // This must be last, as it resets everything.
        s6.add(Setting.getSettingFromKey("ResetEverythingSetting"));

        setting_categories.add(new SettingCategory(c6, s6));
        settings_category_display_names.add("Reset Data");
    }

    public SettingCategory(String displayName, ArrayList<Setting> settingArrayList) {
        this.displayName = displayName;
        this.settingArrayList = settingArrayList;
    }
}
