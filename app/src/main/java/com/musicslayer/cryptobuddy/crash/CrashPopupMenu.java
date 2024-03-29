package com.musicslayer.cryptobuddy.crash;

import android.app.Activity;
import android.content.Context;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import com.musicslayer.cryptobuddy.dialog.CrashReporterDialog;
import com.musicslayer.cryptobuddy.dialog.CrashReporterDialogFragment;
import com.musicslayer.cryptobuddy.util.ContextUtil;
import com.musicslayer.cryptobuddy.util.ThrowableUtil;

abstract public class CrashPopupMenu extends PopupMenu {
    public CrashPopupMenu(Context context, View anchor) {
        super(context, anchor);
    }

    abstract public static class CrashOnMenuItemClickListener implements PopupMenu.OnMenuItemClickListener {
        abstract public boolean onMenuItemClickImpl(MenuItem item);

        public Activity activity;

        public CrashOnMenuItemClickListener(Context context) {
            this.activity = ContextUtil.getActivityFromContext(context);
        }

        @Override
        final public boolean onMenuItemClick(MenuItem item) {
            try {
                return onMenuItemClickImpl(item);
            }
            catch(CrashBypassException e) {
                // Do nothing.
            }
            catch(Exception e) {
                ThrowableUtil.processThrowable(e);

                CrashException crashException = new CrashException(e);
                crashException.setLocationInfo(activity, null);
                crashException.appendExtraInfoFromArgument(item);

                CrashReporterDialogFragment.showCrashDialogFragment(CrashReporterDialog.class, crashException, activity, "crash");
            }

            return true;
        }
    }
}
