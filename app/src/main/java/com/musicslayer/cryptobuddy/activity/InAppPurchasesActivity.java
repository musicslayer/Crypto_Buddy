package com.musicslayer.cryptobuddy.activity;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.crash.CrashRunnable;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.monetization.InAppPurchase;
import com.musicslayer.cryptobuddy.persistence.Purchases;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class InAppPurchasesActivity extends BaseActivity {
    public int getAdLayoutViewID() {
        return -1;
    }

    @Override
    public void onBackPressedImpl() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    public void createLayout () {
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

        Button B_PURCHASE_UNLOCKTOKENS = findViewById(R.id.in_app_purchases_unlockTokensButton);
        B_PURCHASE_UNLOCKTOKENS.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                InAppPurchase.purchaseUnlockTokens(InAppPurchasesActivity.this);
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

        updateLayout();
    }

    public void updateLayout() {
        Button B_PURCHASE_REMOVEDADS = findViewById(R.id.in_app_purchases_removeAdsButton);
        Button B_PURCHASE_UNLOCKTOKENS = findViewById(R.id.in_app_purchases_unlockTokensButton);

        if(Purchases.isRemoveAdsPurchased) {
            B_PURCHASE_REMOVEDADS.setEnabled(false);
            B_PURCHASE_REMOVEDADS.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_check_24, 0, 0, 0);
        }
        else {
            B_PURCHASE_REMOVEDADS.setEnabled(true);
            B_PURCHASE_REMOVEDADS.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_star_24, 0, 0, 0);
        }

        if(Purchases.isUnlockTokensPurchased) {
            B_PURCHASE_UNLOCKTOKENS.setEnabled(false);
            B_PURCHASE_UNLOCKTOKENS.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_check_24, 0, 0, 0);
        }
        else {
            B_PURCHASE_UNLOCKTOKENS.setEnabled(true);
            B_PURCHASE_UNLOCKTOKENS.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_star_24, 0, 0, 0);
        }

        TextView T = findViewById(R.id.in_app_purchases_totalSupportAmountTextView);
        if(Purchases.totalSupportAmount > 0) {
            // Convert Cents to a proper decimal and thank the user.
            BigDecimal bd = new BigDecimal(Purchases.totalSupportAmount).divide(BigDecimal.valueOf(100),2, RoundingMode.UNNECESSARY);
            T.setText("Total Amount: $" + bd.toPlainString() + "\nThank you for your support!");
        }
    }
}