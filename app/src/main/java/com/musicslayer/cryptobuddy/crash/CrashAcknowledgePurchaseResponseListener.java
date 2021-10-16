package com.musicslayer.cryptobuddy.crash;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;

import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingResult;
import com.musicslayer.cryptobuddy.dialog.CrashReporterDialog;
import com.musicslayer.cryptobuddy.dialog.CrashReporterDialogFragment;
import com.musicslayer.cryptobuddy.util.ContextUtil;
import com.musicslayer.cryptobuddy.util.ThrowableUtil;

abstract public class CrashAcknowledgePurchaseResponseListener implements AcknowledgePurchaseResponseListener {
    abstract public void onAcknowledgePurchaseResponseImpl(@NonNull BillingResult billingResult);

    public Activity activity;

    public CrashAcknowledgePurchaseResponseListener(Context context) {
        this.activity = ContextUtil.getActivity(context);
    }

    @Override
    final public void onAcknowledgePurchaseResponse(@NonNull BillingResult billingResult) {
        try {
            onAcknowledgePurchaseResponseImpl(billingResult);
        }
        catch(CrashBypassException e) {
            // Do nothing.
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);

            CrashException crashException = new CrashException(e);
            crashException.setLocationInfo(activity, null);
            crashException.appendExtraInfoFromArgument(billingResult);

            CrashReporterDialogFragment.showCrashDialogFragment(CrashReporterDialog.class, crashException, activity, "crash");
        }
    }
}
