package com.musicslayer.cryptobuddy.crash;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;

import com.musicslayer.cryptobuddy.dialog.CrashDialog;
import com.musicslayer.cryptobuddy.dialog.CrashDialogFragment;
import com.musicslayer.cryptobuddy.util.ContextUtil;
import com.musicslayer.cryptobuddy.util.ThrowableLogger;

abstract public class CrashOnShowListener implements DialogInterface.OnShowListener {
    abstract public void onShowImpl(DialogInterface dialog);

    public Activity activity;

    public CrashOnShowListener(Context context) {
        this.activity = ContextUtil.getActivity(context);
    }

    @Override
    public void onShow(DialogInterface dialog) {
        try {
            onShowImpl(dialog);
        }
        catch(Exception e) {
            ThrowableLogger.processThrowable(e);

            CrashException crashException = new CrashException(e);
            crashException.appendExtraInfoFromArgument(dialog);

            CrashDialogFragment.showCrashDialogFragment(CrashDialog.class, crashException, activity, "crash");
        }
    }
}
