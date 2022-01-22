package com.musicslayer.cryptobuddy.settings.setting;

import android.content.Context;

import com.musicslayer.cryptobuddy.view.settings.DeleteAllAddressPortfoliosSettingsView;
import com.musicslayer.cryptobuddy.view.settings.SettingsView;

import java.util.ArrayList;

public class DeleteAllAddressPortfoliosSetting extends Setting {
    public void updateValue() {}

    public String getKey() { return "DeleteAllAddressPortfoliosSetting"; }
    public String getName() { return "DeleteAllAddressPortfoliosSetting"; }
    public String getDisplayName() { return "Delete All Address Portfolios"; }
    public String getSettingsKey() { return "delete_all_address_portfolios"; }

    public void setSetting(int chosenOptionPosition) {}

    public ArrayList<String> getOptionNames() {
        return null;
    }

    public ArrayList<String> getOptionDisplays() {
        return null;
    }

    public <T> ArrayList<T> getOptionValues() {
        return null;
    }

    public SettingsView createSettingView(Context context) {
        return new DeleteAllAddressPortfoliosSettingsView(context, this);
    }
}
