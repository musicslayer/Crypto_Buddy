package com.musicslayer.cryptobuddy.asset.fiatmanager;

import com.musicslayer.cryptobuddy.asset.fiat.Fiat;
import com.musicslayer.cryptobuddy.asset.fiat.UnknownFiat;

import java.util.ArrayList;
import java.util.HashMap;

public class UnknownFiatManager extends FiatManager {
    String key;

    public String getKey() { return key; }
    public String getName() { return "UnknownFiatManager"; }
    public String getSettingsKey() { return "?"; }

    public static UnknownFiatManager createUnknownFiatManager(String key) {
        return new UnknownFiatManager(key);
    }

    private UnknownFiatManager(String key) {
        this.key = key;

        this.hardcoded_fiats = new ArrayList<>();
        this.hardcoded_fiat_map = new HashMap<>();
        this.hardcoded_fiat_names = new ArrayList<>();
        this.hardcoded_fiat_display_names = new ArrayList<>();

        this.found_fiats = new ArrayList<>();
        this.found_fiat_map = new HashMap<>();
        this.found_fiat_names = new ArrayList<>();
        this.found_fiat_display_names = new ArrayList<>();

        this.custom_fiats = new ArrayList<>();
        this.custom_fiat_map = new HashMap<>();
        this.custom_fiat_names = new ArrayList<>();
        this.custom_fiat_display_names = new ArrayList<>();
    }

    @Override
    // Always return unknown fiats, regardless of if the information is complete.
    public Fiat getFiat(String key, String name, String display_name, int scale) {
        return UnknownFiat.createUnknownFiat(key);
    }
}