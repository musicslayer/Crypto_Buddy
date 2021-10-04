package com.musicslayer.cryptobuddy.crash;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;

import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingResult;
import com.musicslayer.cryptobuddy.dialog.CrashDialog;
import com.musicslayer.cryptobuddy.dialog.CrashDialogFragment;
import com.musicslayer.cryptobuddy.util.ContextUtil;
import com.musicslayer.cryptobuddy.util.ThrowableLogger;

abstract public class CrashAcknowledgePurchaseResponseListener implements AcknowledgePurchaseResponseListener {
    abstract public void onAcknowledgePurchaseResponseImpl(@NonNull BillingResult billingResult);

    public Activity activity;

    public CrashAcknowledgePurchaseResponseListener(Context context) {
        this.activity = ContextUtil.getActivity(context);
    }

    @Override
    public void onAcknowledgePurchaseResponse(@NonNull BillingResult billingResult) {
        try {
            onAcknowledgePurchaseResponseImpl(billingResult);
        }
        catch(Exception e) {
            ThrowableLogger.processThrowable(e);
            CrashDialogFragment.newInstance(CrashDialog.class, e).show(activity, "crash");
        }
    }
}
