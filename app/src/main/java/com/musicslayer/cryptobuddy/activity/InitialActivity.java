package com.musicslayer.cryptobuddy.activity;

import android.content.Context;
import android.content.Intent;

import com.musicslayer.cryptobuddy.api.address.AddressAPI;
import com.musicslayer.cryptobuddy.api.price.PriceAPI;
import com.musicslayer.cryptobuddy.app.App;
import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;
import com.musicslayer.cryptobuddy.asset.fiat.Fiat;
import com.musicslayer.cryptobuddy.asset.network.Network;
import com.musicslayer.cryptobuddy.asset.tokenmanager.TokenManager;
import com.musicslayer.cryptobuddy.persistence.AddressHistory;
import com.musicslayer.cryptobuddy.persistence.AddressPortfolio;
import com.musicslayer.cryptobuddy.persistence.PrivacyPolicy;
import com.musicslayer.cryptobuddy.persistence.Purchases;
import com.musicslayer.cryptobuddy.persistence.Review;
import com.musicslayer.cryptobuddy.persistence.Settings;
import com.musicslayer.cryptobuddy.persistence.TokenManagerList;
import com.musicslayer.cryptobuddy.persistence.TransactionPortfolio;
import com.musicslayer.cryptobuddy.util.ToastUtil;

// This Activity class only exists for initialization code, not to be seen by the user.
// Unlike App.java, this class can show CrashReporterDialog if there is a problem.
public class InitialActivity extends BaseActivity {
    public int getAdLayoutViewID() {
        return -1;
    }

    public void createLayout() {
        // Don't actually show anything. Just do initialization code and then launch MainActivity.
        Context applicationContext = getApplicationContext();

        // Try loading all the persistent data.
        Settings.loadAllSettings(applicationContext);
        ToastUtil.loadAllToasts(applicationContext);
        Fiat.initialize(applicationContext);
        Coin.initialize(applicationContext);
        Network.initialize(applicationContext);
        AddressAPI.initialize(applicationContext);
        PriceAPI.initialize(applicationContext);
        Purchases.loadAllPurchases(applicationContext);
        PrivacyPolicy.loadAllData(applicationContext);
        Review.loadAllData(applicationContext);

        TokenManager.initialize(applicationContext);
        if(!Purchases.isUnlockTokensPurchased) {
            // If the user has not purchased (or has refunded) "Unlock Tokens", we reset the token lists.
            TokenManagerList.resetAllData(applicationContext);
        }

        AddressHistory.loadAllData(applicationContext);
        AddressPortfolio.loadAllData(applicationContext);
        TransactionPortfolio.loadAllData(applicationContext);

        App.isAppInitialized = true;

        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}