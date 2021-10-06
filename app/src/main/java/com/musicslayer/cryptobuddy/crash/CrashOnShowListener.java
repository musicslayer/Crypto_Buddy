package com.musicslayer.cryptobuddy.crash;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;

import com.musicslayer.cryptobuddy.dialog.CrashReporterDialog;
import com.musicslayer.cryptobuddy.dialog.CrashReporterDialogFragment;
import com.musicslayer.cryptobuddy.util.ContextUtil;
import com.musicslayer.cryptobuddy.util.ThrowableUtil;

abstract public class CrashOnShowListener implements DialogInterface.OnShowListener {
    abstract public void onShowImpl(DialogInterface dialog);

    public Activity activity;

    public CrashOnShowListener(Context context) {
        this.activity = ContextUtil.getActivity(context);
    }

    @Override
    final public void onShow(DialogInterface dialog) {
        try {
            onShowImpl(dialog);
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);

            CrashException crashException = new CrashException(e);
            crashException.appendExtraInfoFromArgument(dialog);

            CrashReporterDialogFragment.showCrashDialogFragment(CrashReporterDialog.class, crashException, activity, "crash");
        }
    }
}
