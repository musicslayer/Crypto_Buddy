package com.musicslayer.cryptobuddy.activity;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.ads.AdView;
import com.musicslayer.cryptobuddy.app.App;
import com.musicslayer.cryptobuddy.crash.CrashActivity;
import com.musicslayer.cryptobuddy.crash.CrashRunnable;
import com.musicslayer.cryptobuddy.dialog.BaseDialogFragment;
import com.musicslayer.cryptobuddy.dialog.TransparentDialog;
import com.musicslayer.cryptobuddy.monetization.Ad;
import com.musicslayer.cryptobuddy.monetization.InAppPurchase;
import com.musicslayer.cryptobuddy.data.persistent.app.Purchases;
import com.musicslayer.cryptobuddy.state.StateObj;
import com.musicslayer.cryptobuddy.util.AppearanceUtil;
import com.musicslayer.cryptobuddy.util.TimerUtil;

import java.util.ArrayList;

abstract public class BaseActivity extends CrashActivity {
    // Needed when the current activity is different than the activity captured in a closure.
    private static BaseActivity activity;

    abstract public void createLayout(Bundle savedInstanceState);
    abstract public int getAdLayoutViewID();
    abstract public int getProgressViewID();

    public static BaseActivity getCurrentActivity() {
        return activity;
    }

    public static void setCurrentActivity(BaseActivity activity) {
        BaseActivity.activity = activity;
    }

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

        setCurrentActivity(this);
        AppearanceUtil.setAppearance(this);

        // By default, do nothing when a new purchase is made.
        InAppPurchase.setInAppPurchaseListener(null);

        // On the first creation, reset all state and timers.
        if(savedInstanceState == null) {
            CallbackActivity.resetState();
            StateObj.resetState();
            TimerUtil.reset();
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

    public void runWithProgressIndicator(Runnable startRunnable, Runnable finishRunnable) {
        Thread run = new Thread(new CrashRunnable(this) {
            @Override
            public void runImpl() {
                startRunnable.run();
                hideProgressIndicator();
                runOnUiThread(finishRunnable);
            }
        });

        showProgressIndicator();
        run.start();
    }

    private void showProgressIndicator() {
        BaseDialogFragment.newInstance(TransparentDialog.class).show(this, "transparent");

        if(getProgressViewID() != -1) {
            runOnUiThread(new CrashRunnable(this) {
                @Override
                public void runImpl() {
                    getCurrentActivity().findViewById(getProgressViewID()).setVisibility(View.VISIBLE);
                }
            });
        }
    }

    private void hideProgressIndicator() {
        BaseDialogFragment bdf = (BaseDialogFragment)BaseDialogFragment.getFragmentByTag(getCurrentActivity(), "transparent");
        if(bdf != null) {
            bdf.dismiss();
        }

        if(getProgressViewID() != -1) {
            runOnUiThread(new CrashRunnable(this) {
                @Override
                public void runImpl() {
                    getCurrentActivity().findViewById(getProgressViewID()).setVisibility(View.INVISIBLE);
                }
            });
        }
    }

    public ArrayList<Bitmap> getSurfaceBitmaps() {
        // By default return null, but subclasses can return any surface bitmaps they have.
        return null;
    }

    @Override
    public void onSaveInstanceStateImpl(@NonNull Bundle bundle) {
        if(getProgressViewID() != -1) {
            bundle.putInt("progressVisibility", findViewById(getProgressViewID()).getVisibility());
        }
    }

    @Override
    public void onRestoreInstanceStateImpl(Bundle bundle) {
        if(bundle != null) {
            if(getProgressViewID() != -1) {
                findViewById(getProgressViewID()).setVisibility(bundle.getInt("progressVisibility"));
            }
        }
    }
}
