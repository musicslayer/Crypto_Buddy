package com.musicslayer.cryptobuddy.crash;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;

import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeResponseListener;
import com.musicslayer.cryptobuddy.dialog.CrashReporterDialog;
import com.musicslayer.cryptobuddy.dialog.CrashReporterDialogFragment;
import com.musicslayer.cryptobuddy.util.ContextUtil;
import com.musicslayer.cryptobuddy.util.ThrowableUtil;

abstract public class CrashConsumeResponseListener implements ConsumeResponseListener {
    abstract public void onConsumeResponseImpl(@NonNull BillingResult billingResult, @NonNull String purchaseToken);

    public Activity activity;

    public CrashConsumeResponseListener(Context context) {
        this.activity = ContextUtil.getActivity(context);
    }

    @Override
    final public void onConsumeResponse(@NonNull BillingResult billingResult, @NonNull String purchaseToken) {
        try {
            onConsumeResponseImpl(billingResult, purchaseToken);
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);

            CrashException crashException = new CrashException(e);
            crashException.setLocation(activity, null);
            crashException.appendExtraInfoFromArgument(billingResult);
            crashException.appendExtraInfoFromArgument(purchaseToken);

            CrashReporterDialogFragment.showCrashDialogFragment(CrashReporterDialog.class, crashException, activity, "crash");
        }
    }
}
