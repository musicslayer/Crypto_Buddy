package com.musicslayer.cryptobuddy.crash;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;

import com.musicslayer.cryptobuddy.dialog.CrashReporterDialog;
import com.musicslayer.cryptobuddy.dialog.CrashReporterDialogFragment;
import com.musicslayer.cryptobuddy.util.ContextUtil;
import com.musicslayer.cryptobuddy.util.ThrowableLogger;

abstract public class CrashOnDismissListener implements DialogInterface.OnDismissListener {
    abstract public void onDismissImpl(DialogInterface dialog);

    public Activity activity;

    public CrashOnDismissListener(Context context) {
        this.activity = ContextUtil.getActivity(context);
    }

    @Override
    final public void onDismiss(DialogInterface dialog) {
        try {
            onDismissImpl(dialog);
        }
        catch(Exception e) {
            ThrowableLogger.processThrowable(e);

            CrashException crashException = new CrashException(e);
            crashException.appendExtraInfoFromArgument(dialog);

            CrashReporterDialogFragment.showCrashDialogFragment(CrashReporterDialog.class, crashException, activity, "crash");
        }
    }
}
