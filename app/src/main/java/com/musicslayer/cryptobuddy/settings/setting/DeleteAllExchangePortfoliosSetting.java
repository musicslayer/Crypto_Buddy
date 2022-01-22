package com.musicslayer.cryptobuddy.settings.setting;

import android.content.Context;

import com.musicslayer.cryptobuddy.view.settings.DeleteAllExchangePortfoliosSettingsView;
import com.musicslayer.cryptobuddy.view.settings.SettingsView;

import java.util.ArrayList;

public class DeleteAllExchangePortfoliosSetting extends Setting {
    public void updateValue() {}

    public String getKey() { return "DeleteAllExchangePortfoliosSetting"; }
    public String getName() { return "DeleteAllExchangePortfoliosSetting"; }
    public String getDisplayName() { return "Delete All Exchange Portfolios"; }
    public String getSettingsKey() { return "delete_all_exchange_portfolios"; }

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
        return new DeleteAllExchangePortfoliosSettingsView(context, this);
    }
}
