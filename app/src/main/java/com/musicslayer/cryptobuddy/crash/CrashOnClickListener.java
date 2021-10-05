package com.musicslayer.cryptobuddy.crash;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import com.musicslayer.cryptobuddy.dialog.CrashDialog;
import com.musicslayer.cryptobuddy.dialog.CrashDialogFragment;
import com.musicslayer.cryptobuddy.util.ContextUtil;
import com.musicslayer.cryptobuddy.util.ThrowableLogger;

// TODO In all crash classes, use the object passed in to enhance diagnostics...?

abstract public class CrashOnClickListener implements View.OnClickListener {
    abstract public void onClickImpl(View view);

    public Activity activity;

    public CrashOnClickListener(Context context) {
        this.activity = ContextUtil.getActivity(context);
    }

    @Override
    public void onClick(View view) {
        try {
            onClickImpl(view);
        }
        catch(Exception e) {
            ThrowableLogger.processThrowable(e);

            CrashException crashException = new CrashException(e);
            crashException.appendExtraInfoFromArgument(view);

            CrashDialogFragment.showCrashDialogFragment(CrashDialog.class, crashException, activity, "crash");
        }
    }
}
