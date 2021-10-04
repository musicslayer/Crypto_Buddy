package com.musicslayer.cryptobuddy.crash;

import android.app.Activity;
import android.content.Context;

import androidx.appcompat.widget.SearchView;

import com.musicslayer.cryptobuddy.dialog.CrashDialog;
import com.musicslayer.cryptobuddy.dialog.CrashDialogFragment;
import com.musicslayer.cryptobuddy.util.ContextUtil;
import com.musicslayer.cryptobuddy.util.ThrowableLogger;

abstract public class CrashOnQueryTextListener implements SearchView.OnQueryTextListener {
    abstract public boolean onQueryTextSubmitImpl(String query);
    abstract public boolean onQueryTextChangeImpl(String newText);

    public Activity activity;

    public CrashOnQueryTextListener(Context context) {
        this.activity = ContextUtil.getActivity(context);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        try {
            return onQueryTextSubmitImpl(query);
        }
        catch(Exception e) {
            ThrowableLogger.processThrowable(e);
            CrashDialogFragment.showCrashDialogFragment(CrashDialog.class, e, activity, "crash");
        }

        return false;
    }

    public boolean onQueryTextChange(String newText) {
        try {
            return onQueryTextChangeImpl(newText);
        }
        catch(Exception e) {
            ThrowableLogger.processThrowable(e);
            CrashDialogFragment.showCrashDialogFragment(CrashDialog.class, e, activity, "crash");
        }

        return false;
    }
}
