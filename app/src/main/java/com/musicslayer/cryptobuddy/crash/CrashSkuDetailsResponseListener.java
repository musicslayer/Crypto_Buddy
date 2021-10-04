package com.musicslayer.cryptobuddy.crash;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;

import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.musicslayer.cryptobuddy.dialog.CrashDialog;
import com.musicslayer.cryptobuddy.dialog.CrashDialogFragment;
import com.musicslayer.cryptobuddy.util.ContextUtil;
import com.musicslayer.cryptobuddy.util.ThrowableLogger;

import java.util.List;

abstract public class CrashSkuDetailsResponseListener implements SkuDetailsResponseListener {
    abstract public void onSkuDetailsResponseImpl(@NonNull BillingResult billingResult, List<SkuDetails> skuDetailsList);

    public Activity activity;

    public CrashSkuDetailsResponseListener(Context context) {
        this.activity = ContextUtil.getActivity(context);
    }

    @Override
    public void onSkuDetailsResponse(@NonNull BillingResult billingResult, List<SkuDetails> skuDetailsList) {
        try {
            onSkuDetailsResponseImpl(billingResult, skuDetailsList);
        }
        catch(Exception e) {
            ThrowableLogger.processThrowable(e);
            CrashDialogFragment.showCrashDialogFragment(CrashDialog.class, e, activity, "crash");
        }
    }
}
