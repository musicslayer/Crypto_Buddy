package com.musicslayer.cryptobuddy.crash;

import android.app.Activity;
import android.content.Context;

import androidx.appcompat.widget.SearchView;

import com.musicslayer.cryptobuddy.dialog.CrashReporterDialog;
import com.musicslayer.cryptobuddy.dialog.CrashReporterDialogFragment;
import com.musicslayer.cryptobuddy.util.ContextUtil;
import com.musicslayer.cryptobuddy.util.ThrowableUtil;

abstract public class CrashSearchView extends SearchView {
    public CrashSearchView(Context context) {
        super(context);
    }

    abstract public static class CrashOnQueryTextListener implements SearchView.OnQueryTextListener {
        abstract public boolean onQueryTextSubmitImpl(String query);
        abstract public boolean onQueryTextChangeImpl(String newText);

        public Activity activity;

        public CrashOnQueryTextListener(Context context) {
            this.activity = ContextUtil.getActivity(context);
        }

        @Override
        final public boolean onQueryTextSubmit(String query) {
            try {
                return onQueryTextSubmitImpl(query);
            }
            catch(Exception e) {
                ThrowableUtil.processThrowable(e);

                CrashException crashException = new CrashException(e);
                crashException.setLocationInfo(activity, null);
                crashException.appendExtraInfoFromArgument(query);

                CrashReporterDialogFragment.showCrashDialogFragment(CrashReporterDialog.class, crashException, activity, "crash");
            }

            return false;
        }

        final public boolean onQueryTextChange(String newText) {
            try {
                return onQueryTextChangeImpl(newText);
            }
            catch(Exception e) {
                ThrowableUtil.processThrowable(e);

                CrashException crashException = new CrashException(e);
                crashException.setLocationInfo(activity, null);
                crashException.appendExtraInfoFromArgument(newText);

                CrashReporterDialogFragment.showCrashDialogFragment(CrashReporterDialog.class, crashException, activity, "crash");
            }

            return false;
        }
    }
}
