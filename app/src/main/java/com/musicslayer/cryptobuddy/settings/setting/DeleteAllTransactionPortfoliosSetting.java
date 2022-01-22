package com.musicslayer.cryptobuddy.settings.setting;

import android.content.Context;

import com.musicslayer.cryptobuddy.view.settings.DeleteAllTransactionPortfoliosSettingsView;
import com.musicslayer.cryptobuddy.view.settings.SettingsView;

import java.util.ArrayList;

public class DeleteAllTransactionPortfoliosSetting extends Setting {
    public void updateValue() {}

    public String getKey() { return "DeleteAllTransactionPortfoliosSetting"; }
    public String getName() { return "DeleteAllTransactionPortfoliosSetting"; }
    public String getDisplayName() { return "Delete All Transaction Portfolios"; }
    public String getSettingsKey() { return "delete_all_transaction_portfolios"; }

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
        return new DeleteAllTransactionPortfoliosSettingsView(context, this);
    }
}
