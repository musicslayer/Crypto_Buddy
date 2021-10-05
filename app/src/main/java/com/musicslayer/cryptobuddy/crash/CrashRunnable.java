package com.musicslayer.cryptobuddy.crash;

import android.app.Activity;
import android.content.Context;

import com.musicslayer.cryptobuddy.dialog.CrashDialog;
import com.musicslayer.cryptobuddy.dialog.CrashDialogFragment;
import com.musicslayer.cryptobuddy.util.ContextUtil;
import com.musicslayer.cryptobuddy.util.ThrowableLogger;

abstract public class CrashRunnable implements Runnable {
    abstract public void runImpl();

    public Activity activity;

    public CrashRunnable(Context context) {
        this.activity = ContextUtil.getActivity(context);
    }

    @Override
    public void run() {
        try {
            runImpl();
        }
        catch(Exception e) {
            ThrowableLogger.processThrowable(e);

            CrashException crashException = new CrashException(e);

            CrashDialogFragment.showCrashDialogFragment(CrashDialog.class, crashException, activity, "crash");
        }
    }
}
