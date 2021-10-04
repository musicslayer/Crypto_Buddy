package com.musicslayer.cryptobuddy.crash;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;

import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingResult;
import com.musicslayer.cryptobuddy.dialog.CrashDialog;
import com.musicslayer.cryptobuddy.dialog.CrashDialogFragment;
import com.musicslayer.cryptobuddy.util.ContextUtil;
import com.musicslayer.cryptobuddy.util.ThrowableLogger;

abstract public class CrashBillingClientStateListener implements BillingClientStateListener {
    abstract public void onBillingSetupFinishedImpl(@NonNull BillingResult billingResult);
    abstract public void onBillingServiceDisconnectedImpl();

    public Activity activity;

    public CrashBillingClientStateListener(Context context) {
        this.activity = ContextUtil.getActivity(context);
    }

    @Override
    public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
        try {
            onBillingSetupFinishedImpl(billingResult);
        }
        catch(Exception e) {
            ThrowableLogger.processThrowable(e);
            CrashDialogFragment.newInstance(CrashDialog.class, e).show(activity, "crash");
        }
    }

    @Override
    public void onBillingServiceDisconnected() {
        try {
            onBillingServiceDisconnectedImpl();
        }
        catch(Exception e) {
            ThrowableLogger.processThrowable(e);
            CrashDialogFragment.newInstance(CrashDialog.class, e).show(activity, "crash");
        }
    }
}
