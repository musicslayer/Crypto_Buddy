package com.musicslayer.cryptobuddy.monetization;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.musicslayer.cryptobuddy.app.App;

public class Ad {
    public static boolean areAdsLoaded = false;

    public static void initializeAds(Context context) {
        if(!areAdsLoaded) {
            MobileAds.initialize(context, new OnInitializationCompleteListener() {
                @Override
                public void onInitializationComplete(@NonNull InitializationStatus initializationStatus) {
                    areAdsLoaded = true;
                }
            });
        }
    }

    public static AdView createBannerAdView(Context context) {
        AdView mAdView = new AdView(context);
        mAdView.setAdSize(AdSize.BANNER);

        if(App.DEBUG) {
            mAdView.setAdUnitId("ca-app-pub-3940256099942544/6300978111"); // TEST
        }
        else {
            mAdView.setAdUnitId("ca-app-pub-7875443444219421/2037783782"); // REAL
        }

        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        return mAdView;
    }
}
