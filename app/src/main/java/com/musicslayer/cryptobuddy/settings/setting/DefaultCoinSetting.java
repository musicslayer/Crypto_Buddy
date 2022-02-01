package com.musicslayer.cryptobuddy.settings.setting;

import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;

import java.util.ArrayList;

public class DefaultCoinSetting extends Setting {
    public static Coin value;
    public void updateValue() { value = (Coin)getOptionValues().get(chosenOptionPosition); }

    public String getKey() { return "DefaultCoinSetting"; }
    public String getName() { return "DefaultCoinSetting"; }
    public String getDisplayName() { return "Default Coin"; }
    public String getSettingsKey() { return "default_coin"; }

    public ArrayList<String> getOptionNames() {
        ArrayList<String> optionNames = new ArrayList<>();

        for(Coin coin : Coin.coins) {
            optionNames.add(coin.getDisplayName() + " (" + coin.getName() + ")");
        }

        return optionNames;
    }

    public ArrayList<String> getOptionDisplays() {
        ArrayList<String> optionDisplays = new ArrayList<>();

        for(Coin coin : Coin.coins) {
            optionDisplays.add("Use " + coin.getDisplayName() + " (" + coin.getName() + ") by default.");
        }

        return optionDisplays;
    }

    @SuppressWarnings("unchecked")
    public <T> ArrayList<T> getOptionValues() {
        ArrayList<Coin> optionValues = new ArrayList<>(Coin.coins);
        return (ArrayList<T>)optionValues;
    }
}
