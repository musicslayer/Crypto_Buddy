package com.musicslayer.cryptobuddy.crash;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;

import com.musicslayer.cryptobuddy.dialog.CrashDialog;
import com.musicslayer.cryptobuddy.dialog.CrashDialogFragment;
import com.musicslayer.cryptobuddy.util.ContextUtil;
import com.musicslayer.cryptobuddy.util.ThrowableLogger;

abstract public class CrashOnDismissListener implements DialogInterface.OnDismissListener {
    abstract public void onDismissImpl(DialogInterface dialog);

    public Activity activity;

    public CrashOnDismissListener(Context context) {
        this.activity = ContextUtil.getActivity(context);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        try {
            onDismissImpl(dialog);
        }
        catch(Exception e) {
            ThrowableLogger.processThrowable(e);
            CrashDialogFragment.newInstance(CrashDialog.class, e).show(activity, "crash");
        }
    }
}
