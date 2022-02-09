package com.musicslayer.cryptobuddy.settings.setting;

import com.musicslayer.cryptobuddy.asset.coinmanager.CoinManager;
import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;

import java.util.ArrayList;

public class DefaultCoinSetting extends Setting {
    public static Coin value;
    public void updateValue() { value = getSettingValue(); }

    public String getKey() { return "DefaultCoinSetting"; }
    public String getName() { return "DefaultCoinSetting"; }
    public String getDisplayName() { return "Default Coin"; }
    public String getSettingsKey() { return "default_coin"; }

    public ArrayList<String> getOptionNames() {
        ArrayList<String> optionNames = new ArrayList<>();

        CoinManager coinManager = CoinManager.getDefaultCoinManager();
        for(Coin coin : coinManager.getCoins()) {
            optionNames.add(coin.getComboName());
        }

        return optionNames;
    }

    public String getDefaultOptionName() {
        // Assume that BTC is always available.
        Coin coin = CoinManager.getDefaultCoinManager().getHardcodedCoin("BTC");
        return coin.getComboName();
    }

    public ArrayList<String> getOptionDisplays() {
        ArrayList<String> optionDisplays = new ArrayList<>();

        CoinManager coinManager = CoinManager.getDefaultCoinManager();
        for(Coin coin : coinManager.getCoins()) {
            optionDisplays.add("Use " + coin.getComboName() + " by default.");
        }

        return optionDisplays;
    }

    @SuppressWarnings("unchecked")
    public <T> ArrayList<T> getOptionValues() {
        CoinManager coinManager = CoinManager.getDefaultCoinManager();
        ArrayList<Coin> optionValues = new ArrayList<>(coinManager.getCoins());
        return (ArrayList<T>)optionValues;
    }
}
