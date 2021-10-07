package com.musicslayer.cryptobuddy.crash;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;

import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.musicslayer.cryptobuddy.dialog.CrashReporterDialog;
import com.musicslayer.cryptobuddy.dialog.CrashReporterDialogFragment;
import com.musicslayer.cryptobuddy.util.ContextUtil;
import com.musicslayer.cryptobuddy.util.ThrowableUtil;

import java.util.List;

abstract public class CrashSkuDetailsResponseListener implements SkuDetailsResponseListener {
    abstract public void onSkuDetailsResponseImpl(@NonNull BillingResult billingResult, List<SkuDetails> skuDetailsList);

    public Activity activity;

    public CrashSkuDetailsResponseListener(Context context) {
        this.activity = ContextUtil.getActivity(context);
    }

    @Override
    final public void onSkuDetailsResponse(@NonNull BillingResult billingResult, List<SkuDetails> skuDetailsList) {
        try {
            onSkuDetailsResponseImpl(billingResult, skuDetailsList);
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);

            CrashException crashException = new CrashException(e);
            crashException.setLocationInfo(activity, null);
            crashException.appendExtraInfoFromArgument(billingResult);
            crashException.appendExtraInfoFromArgument(skuDetailsList);

            CrashReporterDialogFragment.showCrashDialogFragment(CrashReporterDialog.class, crashException, activity, "crash");
        }
    }
}
