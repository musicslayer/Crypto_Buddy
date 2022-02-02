package com.musicslayer.cryptobuddy.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.musicslayer.cryptobuddy.api.address.AddressAPI;
import com.musicslayer.cryptobuddy.api.exchange.ExchangeAPI;
import com.musicslayer.cryptobuddy.api.price.PriceAPI;
import com.musicslayer.cryptobuddy.app.App;
import com.musicslayer.cryptobuddy.asset.coinmanager.CoinManager;
import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;
import com.musicslayer.cryptobuddy.asset.exchange.Exchange;
import com.musicslayer.cryptobuddy.asset.fiat.Fiat;
import com.musicslayer.cryptobuddy.asset.fiatmanager.FiatManager;
import com.musicslayer.cryptobuddy.asset.network.Network;
import com.musicslayer.cryptobuddy.asset.tokenmanager.TokenManager;
import com.musicslayer.cryptobuddy.i18n.TimeZoneManager;
import com.musicslayer.cryptobuddy.monetization.InAppPurchase;
import com.musicslayer.cryptobuddy.persistence.AddressHistory;
import com.musicslayer.cryptobuddy.persistence.AddressPortfolio;
import com.musicslayer.cryptobuddy.persistence.ExchangePortfolio;
import com.musicslayer.cryptobuddy.persistence.Policy;
import com.musicslayer.cryptobuddy.persistence.Purchases;
import com.musicslayer.cryptobuddy.persistence.Review;
import com.musicslayer.cryptobuddy.persistence.TokenManagerList;
import com.musicslayer.cryptobuddy.persistence.TransactionPortfolio;
import com.musicslayer.cryptobuddy.settings.category.SettingsCategory;
import com.musicslayer.cryptobuddy.settings.setting.Setting;
import com.musicslayer.cryptobuddy.util.ToastUtil;

import java.util.Date;

// TODO Long Term Items
//  Data Import/Export
//  NFT Viewer
//  Reflections Calculator
//  Use TradeView API
//  "Tax" View (i.e. Calculate cost basis of transactions)
//  Create collection of bridges (and separate classes) to get access to those transactions (for example: MATIC Proof of Stake Bridge).

// TODO MultiWallet (TrustWallet) View (Merge with AddressPortfolio?);
// TODO Actually implement Coinbase/Gemini API.

// TODO Update the "Reset Data" section of the settings for resetting Fiats and Coins.

// This Activity class only exists for initialization code, not to be seen by the user.
// Unlike App.java, this class can show CrashReporterDialog if there is a problem.
public class InitialActivity extends BaseActivity {
    @Override
    public int getAdLayoutViewID() {
        return -1;
    }

    @Override
    public void createLayout(Bundle savedInstanceState) {
        // Don't actually show anything. Just do initialization code and then launch MainActivity.
        Context applicationContext = getApplicationContext();

        // Set time zone base date.
        TimeZoneManager.nowInstant = new Date().toInstant();

        // Try loading all the persistent data.
        // TODO Untangle the web of dependencies.
        Fiat.initialize(applicationContext);
        Coin.initialize(applicationContext);
        Exchange.initialize(applicationContext);
        AddressAPI.initialize(applicationContext);
        PriceAPI.initialize(applicationContext);
        ExchangeAPI.initialize(applicationContext);
        FiatManager.initialize(applicationContext);
        CoinManager.initialize(applicationContext);
        Network.initialize(applicationContext);
        Setting.initialize(applicationContext);
        ToastUtil.loadAllToasts(applicationContext);
        Purchases.loadAllPurchases(applicationContext);
        Policy.loadAllData(applicationContext);
        Review.loadAllData(applicationContext);
        SettingsCategory.initialize(applicationContext);

        InAppPurchase.setWrapperPurchasesUpdatedListener(this);
        InAppPurchase.setWrapperUpdateAllPurchasesListener(this);
        InAppPurchase.initialize(applicationContext);

        TokenManager.initialize(applicationContext);
        if(!Purchases.isUnlockTokensPurchased()) {
            // If the user has not purchased (or has refunded) "Unlock Tokens", we reset the token lists.
            TokenManagerList.resetAllData(applicationContext);
        }

        AddressHistory.loadAllData(applicationContext);
        AddressPortfolio.loadAllData(applicationContext);
        ExchangePortfolio.loadAllData(applicationContext);
        TransactionPortfolio.loadAllData(applicationContext);

        App.isAppInitialized = true;

        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}