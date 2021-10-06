package com.musicslayer.cryptobuddy.crash;

import android.app.Activity;
import android.content.Context;

import com.musicslayer.cryptobuddy.dialog.CrashReporterDialog;
import com.musicslayer.cryptobuddy.dialog.CrashReporterDialogFragment;
import com.musicslayer.cryptobuddy.util.ContextUtil;
import com.musicslayer.cryptobuddy.util.ThrowableUtil;

abstract public class CrashRunnable implements Runnable {
    abstract public void runImpl();

    public Activity activity;

    public CrashRunnable(Context context) {
        this.activity = ContextUtil.getActivity(context);
    }

    @Override
    final public void run() {
        try {
            runImpl();
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);

            CrashException crashException = new CrashException(e);

            CrashReporterDialogFragment.showCrashDialogFragment(CrashReporterDialog.class, crashException, activity, "crash");
        }
    }
}
