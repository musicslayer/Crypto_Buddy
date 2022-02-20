package com.musicslayer.cryptobuddy.activity;

import androidx.constraintlayout.widget.ConstraintLayout;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.ads.AdView;
import com.musicslayer.cryptobuddy.app.App;
import com.musicslayer.cryptobuddy.crash.CrashActivity;
import com.musicslayer.cryptobuddy.monetization.Ad;
import com.musicslayer.cryptobuddy.monetization.InAppPurchase;
import com.musicslayer.cryptobuddy.data.persistent.app.Purchases;
import com.musicslayer.cryptobuddy.state.StateObj;
import com.musicslayer.cryptobuddy.util.AppearanceUtil;

abstract public class BaseActivity extends CrashActivity {
    abstract public void createLayout(Bundle savedInstanceState);
    abstract public int getAdLayoutViewID();

    @Override
    public void onCreateImpl(Bundle savedInstanceState) {
        // In some situations (like manually removing a permission), the app may be "reset" and left in a bad state.
        // We need to exit the app and tell the user to restart.
        if(!App.isAppInitialized) {
            if(this instanceof InitialActivity) {
                // InitialActivity is the entry point of the app and is allowed to initialize.
                ((InitialActivity)this).initialize();
            }
            else {
                // If we get here, we are uninitialized, but we are not at the entry point of the app. Something went wrong!

                // The Toast database is not initialized, so manually create this toast.
                Toast.makeText(this, "Crypto Buddy needs to be restarted.", android.widget.Toast.LENGTH_LONG).show();

                try {
                    // May throw Exceptions, but we don't care. We just need the app to eventually exit.
                    finish();
                }
                catch(Exception ignored) {
                }

                return;
            }
        }

        AppearanceUtil.setAppearance(this);

        // By default, do nothing when a new purchase is made.
        InAppPurchase.setInAppPurchaseListener(null);

        if(savedInstanceState == null) {
            CallbackActivity.wasCallbackFired[0] = false;
            CallbackActivity.lastIntent[0] = null;
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
            Ad.initializeAds();
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
