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
import com.musicslayer.cryptobuddy.persistence.TokenList;
import com.musicslayer.cryptobuddy.persistence.TransactionPortfolio;
import com.musicslayer.cryptobuddy.util.Toast;

// This Activity class only exists for initialization code, not to be seen by the user.
// Unlike in App.java, if there is a crash here we can show CrashReporterDialog.
public class InitialActivity extends BaseActivity {
    public int getAdLayoutViewID() {
        return -1;
    }

    @Override
    public void onBackPressedImpl() {
        // Make sure the user can't cancel out of initialization.
    }

    public void createLayout() {
        // Don't actually show anything. Just do initialization code and then launch MainActivity.

        // Try loading all the persistent data.
        Context applicationContext = getApplicationContext();

        Settings.loadAllSettings(applicationContext);
        Toast.loadAllToasts(applicationContext);
        Fiat.initialize(applicationContext);
        Coin.initialize(applicationContext);
        Network.initialize(applicationContext);
        AddressAPI.initialize(applicationContext);
        PriceAPI.initialize(applicationContext);
        Purchases.loadAllPurchases(applicationContext);
        PrivacyPolicy.loadAllData(applicationContext);
        Review.loadAllData(applicationContext);

        TokenManager.initialize(applicationContext); // * Deserializes, but uses a separate system which catches errors.
        if(!Purchases.isUnlockTokensPurchased) {
            // If the user has not purchased (or has refunded) "Unlock Tokens", we reset the token lists.
            TokenList.resetAllData(applicationContext);
        }

        AddressHistory.loadAllData(applicationContext); // * Deserializes
        AddressPortfolio.loadAllData(applicationContext); // * Deserializes
        TransactionPortfolio.loadAllData(applicationContext); // * Deserializes

        App.isAppInitialized = true;

        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}