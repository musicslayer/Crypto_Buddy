package com.musicslayer.cryptobuddy.crash;

import android.app.Activity;
import android.content.Context;
import android.view.MenuItem;
import android.widget.PopupMenu;

import com.musicslayer.cryptobuddy.dialog.CrashReporterDialog;
import com.musicslayer.cryptobuddy.dialog.CrashReporterDialogFragment;
import com.musicslayer.cryptobuddy.util.ContextUtil;
import com.musicslayer.cryptobuddy.util.ThrowableLogger;

abstract public class CrashOnMenuItemClickListener implements PopupMenu.OnMenuItemClickListener {
    abstract public boolean onMenuItemClickImpl(MenuItem item);

    public Activity activity;

    public CrashOnMenuItemClickListener(Context context) {
        this.activity = ContextUtil.getActivity(context);
    }

    @Override
    final public boolean onMenuItemClick(MenuItem item) {
        try {
            return onMenuItemClickImpl(item);
        }
        catch(Exception e) {
            ThrowableLogger.processThrowable(e);

            CrashException crashException = new CrashException(e);
            crashException.appendExtraInfoFromArgument(item);

            CrashReporterDialogFragment.showCrashDialogFragment(CrashReporterDialog.class, crashException, activity, "crash");
        }

        return true;
    }
}
