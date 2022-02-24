package com.musicslayer.cryptobuddy.activity;

import android.content.Intent;
import android.os.Bundle;

import com.musicslayer.cryptobuddy.api.address.AddressAPI;
import com.musicslayer.cryptobuddy.api.exchange.ExchangeAPI;
import com.musicslayer.cryptobuddy.api.price.PriceAPI;
import com.musicslayer.cryptobuddy.app.App;
import com.musicslayer.cryptobuddy.asset.coinmanager.CoinManager;
import com.musicslayer.cryptobuddy.asset.exchange.Exchange;
import com.musicslayer.cryptobuddy.asset.fiatmanager.FiatManager;
import com.musicslayer.cryptobuddy.asset.network.Network;
import com.musicslayer.cryptobuddy.asset.tokenmanager.TokenManager;
import com.musicslayer.cryptobuddy.data.bridge.DataBridge;
import com.musicslayer.cryptobuddy.data.persistent.app.PersistentAppDataStore;
import com.musicslayer.cryptobuddy.i18n.TimeZoneManager;
import com.musicslayer.cryptobuddy.data.persistent.user.PersistentUserDataStore;
import com.musicslayer.cryptobuddy.settings.category.SettingsCategory;
import com.musicslayer.cryptobuddy.settings.setting.Setting;
import com.musicslayer.cryptobuddy.util.ToastUtil;

import java.util.Date;

// TODO Long Term Items
//  NFT Viewer
//  Use TradeView API
//  "Tax" View (i.e. Calculate cost basis of transactions)
//  Create collection of bridges (and separate classes) to get access to those transactions (for example: MATIC Proof of Stake Bridge).
//  Chart Explorer, Chart Portfolio
//  Fake trades?
//  User accounts?

// TODO Actually implement Coinbase/Gemini API.
// TODO Finish the getSingleAllData Implementations.

// This Activity class only exists for initialization code, not to be seen by the user.
// Unlike App.java, this class can show CrashReporterDialog if there is a problem.
public class InitialActivity extends BaseActivity {
    @Override
    public int getAdLayoutViewID() {
        return -1;
    }

    @Override
    public int getProgressViewID() {
        return -1;
    }

    @Override
    public void createLayout(Bundle savedInstanceState) {
        // Don't actually show anything. This activity exists because it is the only one allowed to perform initialization.
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    public void initialize() {
        // Set time zone base date.
        TimeZoneManager.nowInstant = new Date().toInstant();

        // Initialize all the local app objects.
        PersistentAppDataStore.initialize();
        PersistentUserDataStore.initialize();
        FiatManager.initialize();
        CoinManager.initialize();
        TokenManager.initialize();
        Exchange.initialize();
        Network.initialize();
        AddressAPI.initialize();
        ExchangeAPI.initialize();
        PriceAPI.initialize();
        SettingsCategory.initialize();
        Setting.initialize();
        ToastUtil.initialize();

        // Load all the stored data into local memory.
        PersistentAppDataStore.loadAllStoredData();
        PersistentUserDataStore.loadAllStoredData();

        // REMOVE
        // At this point, everyone should use newer Serialization.
        DataBridge.setIsLegacy(false);

        // Save all the stored data right after loading it.
        // This makes sure the stored data is initialized and helps remove data with outdated versions.
        PersistentAppDataStore.saveAllStoredData();
        PersistentUserDataStore.saveAllStoredData();

        App.isAppInitialized = true;
    }
}