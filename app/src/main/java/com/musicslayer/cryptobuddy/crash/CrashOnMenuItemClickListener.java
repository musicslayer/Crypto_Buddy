package com.musicslayer.cryptobuddy.crash;

import android.app.Activity;
import android.content.Context;
import android.view.MenuItem;
import android.widget.PopupMenu;

import com.musicslayer.cryptobuddy.dialog.CrashDialog;
import com.musicslayer.cryptobuddy.dialog.CrashDialogFragment;
import com.musicslayer.cryptobuddy.util.ContextUtil;
import com.musicslayer.cryptobuddy.util.ThrowableLogger;

abstract public class CrashOnMenuItemClickListener implements PopupMenu.OnMenuItemClickListener {
    abstract public boolean onMenuItemClickImpl(MenuItem item);

    public Activity activity;

    public CrashOnMenuItemClickListener(Context context) {
        this.activity = ContextUtil.getActivity(context);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        try {
            return onMenuItemClickImpl(item);
        }
        catch(Exception e) {
            ThrowableLogger.processThrowable(e);
            CrashDialogFragment.showCrashDialogFragment(CrashDialog.class, e, activity, "crash");
        }

        return true;
    }
}
