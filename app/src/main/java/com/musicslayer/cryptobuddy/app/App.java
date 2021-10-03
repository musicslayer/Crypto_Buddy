package com.musicslayer.cryptobuddy.app;

import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.multidex.MultiDexApplication;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.security.ProviderInstaller;
import com.musicslayer.cryptobuddy.api.address.AddressAPI;
import com.musicslayer.cryptobuddy.api.price.PriceAPI;
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
import com.musicslayer.cryptobuddy.util.DataDump;
import com.musicslayer.cryptobuddy.util.ExceptionLogger;
import com.musicslayer.cryptobuddy.util.Toast;

public class App extends MultiDexApplication {
    public static boolean isGooglePlayAvailable = true;

    @Override
    public void onCreate() {
        super.onCreate();

        // Try loading all the persistent data, and tell user if we cannot.
        // Note that there should be no tolerance for error in any of these functions,
        // so this should only fail if the data was corrupted somehow.
        try {
            Settings.loadAllSettings(this);
            Toast.loadAllToasts(this);
            Fiat.initialize(this);
            Coin.initialize(this);
            Network.initialize(this);
            AddressAPI.initialize(this);
            PriceAPI.initialize(this);
            Purchases.loadAllPurchases(this);
            PrivacyPolicy.loadAllData(this);
            Review.loadAllData(this);

            TokenManager.initialize(this); // * Deserializes, but uses a separate system which catches errors.
            if(!Purchases.isUnlockTokensPurchased) {
                // If the user has not purchased (or has refunded) "Unlock Tokens", we reset the token lists.
                TokenList.resetAllData(this);
            }

            AddressHistory.loadAllData(this); // * Deserializes
            AddressPortfolio.loadAllData(this); // * Deserializes
            TransactionPortfolio.loadAllData(this); // * Deserializes
        }
        catch(Exception e) {
            ExceptionLogger.processException(e);
            Log.e("Crypto Buddy", DataDump.getAllData(null));
        }

        // Log.e("Crypto Buddy", DataDump.getAllData(null));

        // Needed for older Android versions
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        try {
            ProviderInstaller.installIfNeeded(this);
        } catch (GooglePlayServicesRepairableException e) {
            isGooglePlayAvailable = false;
        } catch (GooglePlayServicesNotAvailableException e) {
            isGooglePlayAvailable = false;
        }
    }
}
