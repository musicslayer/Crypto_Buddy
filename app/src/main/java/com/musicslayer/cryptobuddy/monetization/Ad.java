package com.musicslayer.cryptobuddy.monetization;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.musicslayer.cryptobuddy.BuildConfig;
import com.musicslayer.cryptobuddy.app.App;

public class Ad {
    public static boolean areAdsLoaded = false;

    public static void initializeAds() {
        if(!areAdsLoaded) {
            MobileAds.initialize(App.applicationContext, new OnInitializationCompleteListener() {
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
            mAdView.setAdUnitId(BuildConfig.ad_unit_id_test); // TEST
        }
        else {
            mAdView.setAdUnitId(BuildConfig.ad_unit_id_real); // REAL
        }

        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        return mAdView;
    }
}
