package com.musicslayer.cryptobuddy.crash;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;

import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.musicslayer.cryptobuddy.dialog.CrashDialog;
import com.musicslayer.cryptobuddy.dialog.CrashDialogFragment;
import com.musicslayer.cryptobuddy.util.ContextUtil;
import com.musicslayer.cryptobuddy.util.ThrowableLogger;

import java.util.List;

abstract public class CrashPurchasesUpdatedListener implements PurchasesUpdatedListener {
    abstract public void onPurchasesUpdatedImpl(@NonNull BillingResult billingResult, List<Purchase> purchases);

    public Activity activity;

    public CrashPurchasesUpdatedListener(Context context) {
        this.activity = ContextUtil.getActivity(context);
    }

    @Override
    public void onPurchasesUpdated(@NonNull BillingResult billingResult, List<Purchase> purchases) {
        try {
            onPurchasesUpdatedImpl(billingResult, purchases);
        }
        catch(Exception e) {
            ThrowableLogger.processThrowable(e);

            CrashException crashException = new CrashException(e);
            crashException.appendExtraInfoFromArgument(billingResult);
            crashException.appendExtraInfoFromArgument(purchases);

            CrashDialogFragment.showCrashDialogFragment(CrashDialog.class, crashException, activity, "crash");
        }
    }
}
