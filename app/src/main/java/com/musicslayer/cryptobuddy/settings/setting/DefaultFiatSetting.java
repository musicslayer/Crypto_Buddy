package com.musicslayer.cryptobuddy.settings.setting;

import com.musicslayer.cryptobuddy.asset.fiat.Fiat;

import java.util.ArrayList;

public class DefaultFiatSetting extends Setting {
    public static Fiat value;
    public void updateValue() { value = (Fiat)getOptionValues().get(chosenOptionPosition); }

    public String getKey() { return "DefaultFiatSetting"; }
    public String getName() { return "DefaultFiatSetting"; }
    public String getDisplayName() { return "Default Fiat"; }
    public String getSettingsKey() { return "default_fiat"; }

    public ArrayList<String> getOptionNames() {
        ArrayList<String> optionNames = new ArrayList<>();

        for(Fiat fiat : Fiat.fiats) {
            optionNames.add(fiat.getDisplayName() + " (" + fiat.getName() + ")");
        }

        return optionNames;
    }

    public ArrayList<String> getOptionDisplays() {
        ArrayList<String> optionDisplays = new ArrayList<>();

        for(Fiat fiat : Fiat.fiats) {
            optionDisplays.add("Use " + fiat.getDisplayName() + " (" + fiat.getName() + ") by default.");
        }

        return optionDisplays;
    }

    @SuppressWarnings("unchecked")
    public <T> ArrayList<T> getOptionValues() {
        ArrayList<Fiat> optionValues = new ArrayList<>(Fiat.fiats);
        return (ArrayList<T>)optionValues;
    }
}
