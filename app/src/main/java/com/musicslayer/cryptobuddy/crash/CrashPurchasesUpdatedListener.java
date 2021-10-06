package com.musicslayer.cryptobuddy.crash;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;

import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.musicslayer.cryptobuddy.dialog.CrashReporterDialog;
import com.musicslayer.cryptobuddy.dialog.CrashReporterDialogFragment;
import com.musicslayer.cryptobuddy.util.ContextUtil;
import com.musicslayer.cryptobuddy.util.ThrowableUtil;

import java.util.List;

abstract public class CrashPurchasesUpdatedListener implements PurchasesUpdatedListener {
    abstract public void onPurchasesUpdatedImpl(@NonNull BillingResult billingResult, List<Purchase> purchases);

    public Activity activity;

    public CrashPurchasesUpdatedListener(Context context) {
        this.activity = ContextUtil.getActivity(context);
    }

    @Override
    final public void onPurchasesUpdated(@NonNull BillingResult billingResult, List<Purchase> purchases) {
        try {
            onPurchasesUpdatedImpl(billingResult, purchases);
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);

            CrashException crashException = new CrashException(e);
            crashException.appendExtraInfoFromArgument(billingResult);
            crashException.appendExtraInfoFromArgument(purchases);

            CrashReporterDialogFragment.showCrashDialogFragment(CrashReporterDialog.class, crashException, activity, "crash");
        }
    }
}
