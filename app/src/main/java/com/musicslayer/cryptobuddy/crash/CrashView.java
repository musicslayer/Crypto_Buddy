package com.musicslayer.cryptobuddy.crash;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import com.musicslayer.cryptobuddy.dialog.CrashReporterDialog;
import com.musicslayer.cryptobuddy.dialog.CrashReporterDialogFragment;
import com.musicslayer.cryptobuddy.util.ContextUtil;
import com.musicslayer.cryptobuddy.util.ThrowableUtil;

abstract public class CrashView extends View {
    public CrashView(Context context) {
        super(context);
    }

    abstract public static class CrashOnClickListener implements View.OnClickListener {
        abstract public void onClickImpl(View view);

        public Activity activity;

        public CrashOnClickListener(Context context) {
            this.activity = ContextUtil.getActivityFromContext(context);
        }

        @Override
        final public void onClick(View view) {
            try {
                onClickImpl(view);
            }
            catch(CrashBypassException e) {
                // Do nothing.
            }
            catch(Exception e) {
                ThrowableUtil.processThrowable(e);

                CrashException crashException = new CrashException(e);
                crashException.setLocationInfo(activity, view);
                crashException.appendExtraInfoFromArgument(view);

                CrashReporterDialogFragment.showCrashDialogFragment(CrashReporterDialog.class, crashException, activity, "crash");
            }
        }
    }
}
