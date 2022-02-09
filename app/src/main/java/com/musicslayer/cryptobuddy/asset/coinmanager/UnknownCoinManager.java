package com.musicslayer.cryptobuddy.asset.coinmanager;

import android.content.Context;

import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;
import com.musicslayer.cryptobuddy.asset.crypto.coin.UnknownCoin;

import java.util.ArrayList;
import java.util.HashMap;

public class UnknownCoinManager extends CoinManager {
    String key;
    String coinType;

    public String getKey() { return key; }
    public String getName() { return "UnknownCoinManager"; }
    public String getCoinType() { return coinType; }
    public String getSettingsKey() { return "?"; }
    public void initializeHardcodedCoins(Context context) {}

    public static UnknownCoinManager createUnknownCoinManager(String key, String coinType) {
        return new UnknownCoinManager(key, coinType);
    }

    private UnknownCoinManager(String key, String coinType) {
        this.key = key;
        this.coinType = coinType;

        this.hardcoded_coins = new ArrayList<>();
        this.hardcoded_coin_map = new HashMap<>();
        this.hardcoded_coin_names = new ArrayList<>();
        this.hardcoded_coin_display_names = new ArrayList<>();

        this.found_coins = new ArrayList<>();
        this.found_coin_map = new HashMap<>();
        this.found_coin_names = new ArrayList<>();
        this.found_coin_display_names = new ArrayList<>();

        this.custom_coins = new ArrayList<>();
        this.custom_coin_map = new HashMap<>();
        this.custom_coin_names = new ArrayList<>();
        this.custom_coin_display_names = new ArrayList<>();
    }

    @Override
    // Always return unknown coins, regardless of if the information is complete.
    public Coin getCoin(String key, String name, String display_name, int scale, String id) {
        return UnknownCoin.createUnknownCoin(key, name, display_name, scale, getCoinType());
    }
}
