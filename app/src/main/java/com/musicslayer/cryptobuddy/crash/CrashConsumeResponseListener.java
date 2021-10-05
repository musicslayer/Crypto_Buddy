package com.musicslayer.cryptobuddy.crash;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;

import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeResponseListener;
import com.musicslayer.cryptobuddy.dialog.CrashDialog;
import com.musicslayer.cryptobuddy.dialog.CrashDialogFragment;
import com.musicslayer.cryptobuddy.util.ContextUtil;
import com.musicslayer.cryptobuddy.util.ThrowableLogger;

abstract public class CrashConsumeResponseListener implements ConsumeResponseListener {
    abstract public void onConsumeResponseImpl(@NonNull BillingResult billingResult, @NonNull String purchaseToken);

    public Activity activity;

    public CrashConsumeResponseListener(Context context) {
        this.activity = ContextUtil.getActivity(context);
    }

    @Override
    public void onConsumeResponse(@NonNull BillingResult billingResult, @NonNull String purchaseToken) {
        try {
            onConsumeResponseImpl(billingResult, purchaseToken);
        }
        catch(Exception e) {
            ThrowableLogger.processThrowable(e);

            CrashException crashException = new CrashException(e);
            crashException.appendExtraInfoFromArgument(billingResult);
            crashException.appendExtraInfoFromArgument(purchaseToken);

            CrashDialogFragment.showCrashDialogFragment(CrashDialog.class, crashException, activity, "crash");
        }
    }
}
