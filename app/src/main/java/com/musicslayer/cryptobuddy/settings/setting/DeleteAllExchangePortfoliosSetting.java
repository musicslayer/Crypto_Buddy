package com.musicslayer.cryptobuddy.settings.setting;

import android.content.Context;

import com.musicslayer.cryptobuddy.view.settings.DeleteAllExchangePortfoliosSettingsView;
import com.musicslayer.cryptobuddy.view.settings.SettingsView;

import java.util.ArrayList;

public class DeleteAllExchangePortfoliosSetting extends Setting {
    public static String value;
    public void updateValue() { value = (String)getOptionValues().get(chosenOptionPosition); }

    public String getKey() { return "DeleteAllExchangePortfoliosSetting"; }
    public String getName() { return "DeleteAllExchangePortfoliosSetting"; }
    public String getDisplayName() { return "Delete All Exchange Portfolios"; }
    public String getSettingsKey() { return "delete_all_exchange_portfolios"; }

    public ArrayList<String> getOptionNames() {
        ArrayList<String> optionNames = new ArrayList<>();
        optionNames.add("?");
        return optionNames;
    }

    public String getDefaultOptionName() {
        return "?";
    }

    public ArrayList<String> getOptionDisplays() {
        ArrayList<String> optionDisplays = new ArrayList<>();
        optionDisplays.add("?");
        return optionDisplays;
    }

    @SuppressWarnings("unchecked")
    public <T> ArrayList<T> getOptionValues() {
        ArrayList<String> optionValues = new ArrayList<>();
        optionValues.add("?");
        return (ArrayList<T>)optionValues;
    }

    public SettingsView createSettingView(Context context) {
        return new DeleteAllExchangePortfoliosSettingsView(context, this);
    }
}
