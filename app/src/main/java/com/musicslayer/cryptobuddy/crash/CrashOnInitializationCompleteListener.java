package com.musicslayer.cryptobuddy.crash;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.musicslayer.cryptobuddy.dialog.CrashDialog;
import com.musicslayer.cryptobuddy.dialog.CrashDialogFragment;
import com.musicslayer.cryptobuddy.util.ContextUtil;
import com.musicslayer.cryptobuddy.util.ThrowableLogger;

abstract public class CrashOnInitializationCompleteListener implements OnInitializationCompleteListener {
    abstract public void onInitializationCompleteImpl(@NonNull InitializationStatus initializationStatus);

    public Activity activity;

    public CrashOnInitializationCompleteListener(Context context) {
        this.activity = ContextUtil.getActivity(context);
    }

    @Override
    public void onInitializationComplete(@NonNull InitializationStatus initializationStatus) {
        try {
            onInitializationCompleteImpl(initializationStatus);
        }
        catch(Exception e) {
            ThrowableLogger.processThrowable(e);

            CrashException crashException = new CrashException(e);
            crashException.appendExtraInfoFromArgument(initializationStatus);

            CrashDialogFragment.showCrashDialogFragment(CrashDialog.class, crashException, activity, "crash");
        }
    }
}
