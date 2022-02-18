package com.musicslayer.cryptobuddy.activity;

import android.content.Context;
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
//  Use TradeView API
//  "Tax" View (i.e. Calculate cost basis of transactions)
//  Create collection of bridges (and separate classes) to get access to those transactions (for example: MATIC Proof of Stake Bridge).
//  Chart Explorer, Chart Portfolio
//  Fake trades?

// TODO Actually implement Coinbase/Gemini API.
// TODO Merge isLoss with BigDecimal math.
// TODO Finish the getSingleAllData Implementations.

// TODO Toggle buttons, each label need same amount of letters.
// TODO If I uninstall, make sure internal and external folders both persist.

// This Activity class only exists for initialization code, not to be seen by the user.
// Unlike App.java, this class can show CrashReporterDialog if there is a problem.
public class InitialActivity extends BaseActivity {
    @Override
    public int getAdLayoutViewID() {
        return -1;
    }

    @Override
    public void createLayout(Bundle savedInstanceState) {
        // Don't actually show anything. This activity exists because it is the only one allowed to perform initialization.
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    public void initialize() {
        Context applicationContext = getApplicationContext();

        // Set time zone base date.
        TimeZoneManager.nowInstant = new Date().toInstant();

        // Purchases should be initialized first, as others may depend on this.
        Purchases.loadAllPurchases(applicationContext);

        // Initialize assets here. This will also overwrite hardcoded assets that were loaded from before.
        FiatManager.initialize(applicationContext);
        CoinManager.initialize(applicationContext);

        // If the user has not purchased (or has refunded) "Unlock Tokens", we reset the token lists.
        TokenManager.initialize(applicationContext);
        if(!Purchases.isUnlockTokensPurchased()) {
            TokenManager.resetAllTokens();
            TokenManagerList.resetAllData(applicationContext);
        }

        Exchange.initialize(applicationContext);
        Network.initialize(applicationContext); // Requires CoinManagers and TokenManagers to have loaded first.
        AddressAPI.initialize(applicationContext);
        ExchangeAPI.initialize(applicationContext);
        PriceAPI.initialize(applicationContext);
        InAppPurchase.initialize(applicationContext); // Requires Purchases
        SettingsCategory.initialize(applicationContext);
        ToastUtil.loadAllToasts(applicationContext);
        AddressHistory.loadAllData(applicationContext);
        AddressPortfolio.loadAllData(applicationContext);
        ExchangePortfolio.loadAllData(applicationContext);
        TransactionPortfolio.loadAllData(applicationContext);
        Policy.loadAllData(applicationContext);
        Review.loadAllData(applicationContext);

        // Settings should be initialized last, as this could theoretically depend on anything.
        Setting.initialize(applicationContext);

        App.isAppInitialized = true;
    }
}