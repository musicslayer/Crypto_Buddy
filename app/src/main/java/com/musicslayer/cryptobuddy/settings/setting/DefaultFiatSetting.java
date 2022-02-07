package com.musicslayer.cryptobuddy.settings.setting;

import com.musicslayer.cryptobuddy.asset.fiat.Fiat;
import com.musicslayer.cryptobuddy.asset.fiat.USD;
import com.musicslayer.cryptobuddy.asset.fiatmanager.FiatManager;

import java.util.ArrayList;

public class DefaultFiatSetting extends Setting {
    public static Fiat value;
    public void updateValue() { value = (Fiat)getSettingValue(); }

    public String getKey() { return "DefaultFiatSetting"; }
    public String getName() { return "DefaultFiatSetting"; }
    public String getDisplayName() { return "Default Fiat"; }
    public String getSettingsKey() { return "default_fiat"; }

    public ArrayList<String> getOptionNames() {
        ArrayList<String> optionNames = new ArrayList<>();

        FiatManager fiatManager = FiatManager.getDefaultFiatManager();
        for(Fiat fiat : fiatManager.getFiats()) {
            optionNames.add(fiat.getDisplayName() + " (" + fiat.getName() + ")");
        }

        return optionNames;
    }

    public String getDefaultOptionName() {
        // Assume that USD is always available.
        Fiat fiat = new USD();
        return fiat.getDisplayName() + " (" + fiat.getName() + ")";
    }

    public ArrayList<String> getOptionDisplays() {
        ArrayList<String> optionDisplays = new ArrayList<>();

        FiatManager fiatManager = FiatManager.getDefaultFiatManager();
        for(Fiat fiat : fiatManager.getFiats()) {
            optionDisplays.add("Use " + fiat.getDisplayName() + " (" + fiat.getName() + ") by default.");
        }

        return optionDisplays;
    }

    @SuppressWarnings("unchecked")
    public <T> ArrayList<T> getOptionValues() {
        FiatManager fiatManager = FiatManager.getDefaultFiatManager();
        ArrayList<Fiat> optionValues = new ArrayList<>(fiatManager.getFiats());
        return (ArrayList<T>)optionValues;
    }
}
