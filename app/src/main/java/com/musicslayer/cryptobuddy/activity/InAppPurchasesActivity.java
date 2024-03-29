package com.musicslayer.cryptobuddy.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.crash.CrashRunnable;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.monetization.InAppPurchase;
import com.musicslayer.cryptobuddy.data.persistent.app.Purchases;
import com.musicslayer.cryptobuddy.util.ToastUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class InAppPurchasesActivity extends BaseActivity {
    @Override
    public int getAdLayoutViewID() {
        return -1;
    }

    @Override
    public int getProgressViewID() {
        return -1;
    }

    @Override
    public void onBackPressedImpl() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    public void createLayout(Bundle savedInstanceState) {
        setContentView(R.layout.activity_in_app_purchases);

        InAppPurchase.setInAppPurchaseListener(new InAppPurchase.InAppPurchaseListener() {
            @Override
            public void onInAppPurchase() {
                // Needed if the purchase update happened on another thread.
                runOnUiThread(new CrashRunnable(InAppPurchasesActivity.this) {
                    @Override
                    public void runImpl() {
                        updateLayout();
                    }
                });
            }
        });

        Toolbar toolbar = findViewById(R.id.in_app_purchases_toolbar);
        setSupportActionBar(toolbar);

        Button B_PURCHASE_REMOVEDADS = findViewById(R.id.in_app_purchases_removeAdsButton);
        B_PURCHASE_REMOVEDADS.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                InAppPurchase.purchaseRemoveAds(InAppPurchasesActivity.this);
            }
        });

        Button B_PURCHASE_UNLOCKPREMIUMFEATURES = findViewById(R.id.in_app_purchases_unlockPremiumFeaturesButton);
        B_PURCHASE_UNLOCKPREMIUMFEATURES.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                InAppPurchase.purchaseUnlockPremiumFeatures(InAppPurchasesActivity.this);
            }
        });

        Button B_PURCHASE_SUPPORTDEVELOPER1 = findViewById(R.id.in_app_purchases_supportDeveloperButton1);
        B_PURCHASE_SUPPORTDEVELOPER1.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                InAppPurchase.purchaseSupportDeveloper1(InAppPurchasesActivity.this);
            }
        });

        Button B_PURCHASE_SUPPORTDEVELOPER2 = findViewById(R.id.in_app_purchases_supportDeveloperButton2);
        B_PURCHASE_SUPPORTDEVELOPER2.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                InAppPurchase.purchaseSupportDeveloper2(InAppPurchasesActivity.this);
            }
        });

        Button B_PURCHASE_SUPPORTDEVELOPER3 = findViewById(R.id.in_app_purchases_supportDeveloperButton3);
        B_PURCHASE_SUPPORTDEVELOPER3.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                InAppPurchase.purchaseSupportDeveloper3(InAppPurchasesActivity.this);
            }
        });

        Button B_RESTORE_PURCHASES = findViewById(R.id.in_app_purchases_restorePurchasesButton);
        B_RESTORE_PURCHASES.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                ToastUtil.showToast("restoring_purchases");
                InAppPurchase.updateAllPurchases();
            }
        });

        updateLayout();
    }

    public void updateLayout() {
        Button B_PURCHASE_REMOVEDADS = findViewById(R.id.in_app_purchases_removeAdsButton);
        Button B_PURCHASE_UNLOCKPREMIUMFEATURES = findViewById(R.id.in_app_purchases_unlockPremiumFeaturesButton);

        if(Purchases.isRemoveAdsPurchased()) {
            B_PURCHASE_REMOVEDADS.setEnabled(false);
            B_PURCHASE_REMOVEDADS.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_check_24, 0, 0, 0);
        }
        else {
            B_PURCHASE_REMOVEDADS.setEnabled(true);
            B_PURCHASE_REMOVEDADS.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_star_24, 0, 0, 0);
        }

        if(Purchases.isUnlockPremiumFeaturesPurchased()) {
            B_PURCHASE_UNLOCKPREMIUMFEATURES.setEnabled(false);
            B_PURCHASE_UNLOCKPREMIUMFEATURES.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_check_24, 0, 0, 0);
        }
        else {
            B_PURCHASE_UNLOCKPREMIUMFEATURES.setEnabled(true);
            B_PURCHASE_UNLOCKPREMIUMFEATURES.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_star_24, 0, 0, 0);
        }

        TextView T = findViewById(R.id.in_app_purchases_totalSupportAmountTextView);
        if(Purchases.getTotalSupportAmount() > 0) {
            // Convert Cents to a proper decimal and thank the user.
            BigDecimal bd = new BigDecimal(Purchases.getTotalSupportAmount()).divide(BigDecimal.valueOf(100),2, RoundingMode.UNNECESSARY);
            T.setVisibility(View.VISIBLE);
            T.setText("Total Amount: $" + bd.toPlainString() + "\nThank you for your support!");
        }
        else {
            T.setVisibility(View.GONE);
        }
    }
}