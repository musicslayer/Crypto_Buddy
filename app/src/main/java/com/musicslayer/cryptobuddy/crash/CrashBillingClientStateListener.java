package com.musicslayer.cryptobuddy.crash;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;

import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingResult;
import com.musicslayer.cryptobuddy.dialog.CrashReporterDialog;
import com.musicslayer.cryptobuddy.dialog.CrashReporterDialogFragment;
import com.musicslayer.cryptobuddy.util.ContextUtil;
import com.musicslayer.cryptobuddy.util.ThrowableUtil;

abstract public class CrashBillingClientStateListener implements BillingClientStateListener {
    abstract public void onBillingSetupFinishedImpl(@NonNull BillingResult billingResult);
    abstract public void onBillingServiceDisconnectedImpl();

    public Activity activity;

    public CrashBillingClientStateListener(Context context) {
        this.activity = ContextUtil.getActivity(context);
    }

    @Override
    final public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
        try {
            onBillingSetupFinishedImpl(billingResult);
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);

            CrashException crashException = new CrashException(e);
            crashException.appendExtraInfoFromArgument(billingResult);

            CrashReporterDialogFragment.showCrashDialogFragment(CrashReporterDialog.class, crashException, activity, "crash");
        }
    }

    @Override
    final public void onBillingServiceDisconnected() {
        try {
            onBillingServiceDisconnectedImpl();
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);

            CrashException crashException = new CrashException(e);

            CrashReporterDialogFragment.showCrashDialogFragment(CrashReporterDialog.class, crashException, activity, "crash");
        }
    }
}
