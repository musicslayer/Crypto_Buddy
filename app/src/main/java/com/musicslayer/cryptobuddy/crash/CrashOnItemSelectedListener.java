package com.musicslayer.cryptobuddy.crash;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.AdapterView;

import com.musicslayer.cryptobuddy.dialog.CrashDialog;
import com.musicslayer.cryptobuddy.dialog.CrashDialogFragment;
import com.musicslayer.cryptobuddy.util.ContextUtil;
import com.musicslayer.cryptobuddy.util.ThrowableLogger;

abstract public class CrashOnItemSelectedListener implements AdapterView.OnItemSelectedListener {
    abstract public void onNothingSelectedImpl(AdapterView<?> parent);
    abstract public void onItemSelectedImpl(AdapterView<?> parent, View view, int pos, long id);

    public Activity activity;

    public CrashOnItemSelectedListener(Context context) {
        this.activity = ContextUtil.getActivity(context);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        try {
            onNothingSelectedImpl(parent);
        }
        catch(Exception e) {
            ThrowableLogger.processThrowable(e);
            CrashDialogFragment.newInstance(CrashDialog.class, e).show(activity, "crash");
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        try {
            onItemSelectedImpl(parent, view, pos, id);
        }
        catch(Exception e) {
            ThrowableLogger.processThrowable(e);
            CrashDialogFragment.newInstance(CrashDialog.class, e).show(activity, "crash");
        }
    }
}
