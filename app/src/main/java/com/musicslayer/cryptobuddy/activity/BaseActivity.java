package com.musicslayer.cryptobuddy.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.os.Bundle;
import android.view.ViewGroup;

import com.google.android.gms.ads.AdView;
import com.musicslayer.cryptobuddy.monetization.Ad;
import com.musicslayer.cryptobuddy.monetization.InAppPurchase;
import com.musicslayer.cryptobuddy.persistence.Purchases;
import com.musicslayer.cryptobuddy.util.Appearance;

abstract public class BaseActivity extends AppCompatActivity {
    abstract public void createLayout();
    abstract public int getAdLayoutViewID();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Appearance.setAppearance(this);

        InAppPurchase.setInAppPurchaseListener(new InAppPurchase.InAppPurchaseListener() {
            @Override
            public void onInAppPurchase() {
            }
        });
        InAppPurchase.initialize(getApplicationContext()); // Keep trying if the call in App class was not successful.

        createLayout();
        adjustActivity();
    }

    public void adjustActivity() {
        // Add the ads if the user did not purchase "Remove Ads".
        if(!Purchases.isRemoveAdsPurchased && getAdLayoutViewID() != -1) {
            Ad.initializeAds(this);
            ViewGroup v = findViewById(getAdLayoutViewID());

            AdView ad = Ad.createBannerAdView(this);
            ConstraintLayout.LayoutParams CL = (ConstraintLayout.LayoutParams)v.getLayoutParams();
            CL.width = ad.getAdSize().getWidthInPixels(this);
            CL.height = ad.getAdSize().getHeightInPixels(this);
            v.setLayoutParams(CL);
            v.addView(ad);
        }
    }
}
