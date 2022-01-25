package com.musicslayer.cryptobuddy.activity;

import androidx.constraintlayout.widget.ConstraintLayout;

import android.os.Bundle;
import android.view.ViewGroup;

import com.google.android.gms.ads.AdView;
import com.musicslayer.cryptobuddy.app.App;
import com.musicslayer.cryptobuddy.crash.CrashActivity;
import com.musicslayer.cryptobuddy.monetization.Ad;
import com.musicslayer.cryptobuddy.monetization.InAppPurchase;
import com.musicslayer.cryptobuddy.persistence.Purchases;
import com.musicslayer.cryptobuddy.state.StateObj;
import com.musicslayer.cryptobuddy.util.AppearanceUtil;

abstract public class BaseActivity extends CrashActivity {
    abstract public void createLayout(Bundle savedInstanceState);
    abstract public int getAdLayoutViewID();

    @Override
    public void onCreateImpl(Bundle savedInstanceState) {
        if(App.isAppInitialized) {
            AppearanceUtil.setAppearance(this);

            InAppPurchase.setInAppPurchaseListener(new InAppPurchase.InAppPurchaseListener() {
                @Override
                public void onInAppPurchase() {}
            });

            InAppPurchase.setInnerPurchasesUpdatedListener(this);
            InAppPurchase.setInnerUpdateAllPurchasesListener(this);

            if(savedInstanceState == null) {
                CallbackActivity.wasCallbackFired[0] = false;
                CallbackActivity.lastIntent[0] = null;
            }
        }

        // Clear state the first time each activity is created.
        if(savedInstanceState == null) {
            StateObj.resetState();
        }

        createLayout(savedInstanceState);
        adjustActivity();
    }

    public void adjustActivity() {
        // Add the ads if the user did not purchase "Remove Ads".
        if(!Purchases.isRemoveAdsPurchased() && getAdLayoutViewID() != -1) {
            Ad.initializeAds(getApplicationContext());
            ViewGroup v = findViewById(getAdLayoutViewID());

            AdView ad = Ad.createBannerAdView(this);
            if(ad.getAdSize() != null) {
                ConstraintLayout.LayoutParams CL = (ConstraintLayout.LayoutParams)v.getLayoutParams();
                CL.width = ad.getAdSize().getWidthInPixels(this);
                CL.height = ad.getAdSize().getHeightInPixels(this);
                v.setLayoutParams(CL);
                v.addView(ad);
            }
        }
    }
}
