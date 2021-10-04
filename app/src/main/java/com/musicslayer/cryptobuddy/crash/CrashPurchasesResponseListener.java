package com.musicslayer.cryptobuddy.crash;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;

import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesResponseListener;
import com.musicslayer.cryptobuddy.dialog.CrashDialog;
import com.musicslayer.cryptobuddy.dialog.CrashDialogFragment;
import com.musicslayer.cryptobuddy.util.ContextUtil;
import com.musicslayer.cryptobuddy.util.ThrowableLogger;

import java.util.List;

abstract public class CrashPurchasesResponseListener implements PurchasesResponseListener {
    abstract public void onQueryPurchasesResponseImpl(@NonNull BillingResult billingResult, @NonNull List<Purchase> purchases);

    public Activity activity;

    public CrashPurchasesResponseListener(Context context) {
        this.activity = ContextUtil.getActivity(context);
    }

    @Override
    public void onQueryPurchasesResponse(@NonNull BillingResult billingResult, @NonNull List<Purchase> purchases) {
        try {
            onQueryPurchasesResponseImpl(billingResult, purchases);
        }
        catch(Exception e) {
            ThrowableLogger.processThrowable(e);
            CrashDialogFragment.showCrashDialogFragment(CrashDialog.class, e, activity, "crash");
        }
    }
}
