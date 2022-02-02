package com.musicslayer.cryptobuddy.settings.setting;

import com.musicslayer.cryptobuddy.asset.coinmanager.CoinManager;
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

        CoinManager coinManager = CoinManager.getCoinManagerFromKey("BaseCoinManager");
        for(Coin coin : coinManager.getCoins()) {
            optionNames.add(coin.getDisplayName() + " (" + coin.getName() + ")");
        }

        return optionNames;
    }

    public ArrayList<String> getOptionDisplays() {
        ArrayList<String> optionDisplays = new ArrayList<>();

        CoinManager coinManager = CoinManager.getCoinManagerFromKey("BaseCoinManager");
        for(Coin coin : coinManager.getCoins()) {
            optionDisplays.add("Use " + coin.getDisplayName() + " (" + coin.getName() + ") by default.");
        }

        return optionDisplays;
    }

    @SuppressWarnings("unchecked")
    public <T> ArrayList<T> getOptionValues() {
        CoinManager coinManager = CoinManager.getCoinManagerFromKey("BaseCoinManager");
        ArrayList<Coin> optionValues = new ArrayList<>(coinManager.getCoins());
        return (ArrayList<T>)optionValues;
    }
}
