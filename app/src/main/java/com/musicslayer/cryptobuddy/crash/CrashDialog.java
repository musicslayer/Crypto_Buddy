package com.musicslayer.cryptobuddy.crash;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.musicslayer.cryptobuddy.dialog.CrashReporterDialog;
import com.musicslayer.cryptobuddy.dialog.CrashReporterDialogFragment;
import com.musicslayer.cryptobuddy.util.ThrowableUtil;

abstract public class CrashDialog extends Dialog {
    private final AppCompatActivity activity;

    public CrashDialog(Activity activity) {
        super(activity);
        this.activity = (AppCompatActivity)activity;
    }

    public boolean canLaunchCrashReporterDialog() {
        // We do not want recursive calling of CrashReporterDialog.
        return !(this instanceof CrashReporterDialog);
    }

    @Override
    final public void onBackPressed() {
        try {
            onBackPressedImpl();
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);

            if(canLaunchCrashReporterDialog()) {
                CrashException crashException = new CrashException(e);
                crashException.setLocation(activity,null);
                CrashReporterDialogFragment.showCrashDialogFragment(CrashReporterDialog.class, crashException, activity, "crash");
            }
        }
    }

    @Override
    final protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            onCreateImpl(savedInstanceState);
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);

            if(canLaunchCrashReporterDialog()) {
                CrashException crashException = new CrashException(e);
                crashException.setLocation(activity,null);
                crashException.appendExtraInfoFromArgument(savedInstanceState);
                CrashReporterDialogFragment.showCrashDialogFragment(CrashReporterDialog.class, crashException, activity, "crash");
            }
        }
    }

    @Override
    final public void show() {
        try {
            super.show();
            showImpl();
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);

            if(canLaunchCrashReporterDialog()) {
                CrashException crashException = new CrashException(e);
                crashException.setLocation(activity,null);
                CrashReporterDialogFragment.showCrashDialogFragment(CrashReporterDialog.class, crashException, activity, "crash");
            }
        }
    }

    @Override
    final public Bundle onSaveInstanceState() {
        try {
            return onSaveInstanceStateImpl(super.onSaveInstanceState());
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);

            // We cannot show the dialog here, so just save the exception.
            Bundle bundle = super.onSaveInstanceState();
            bundle.putSerializable("!EXCEPTION!", e);
            return bundle;
        }
    }

    @Override
    final public void onRestoreInstanceState(Bundle bundle) {
        // If onSaveInstanceState had an exception, just show the crash reporter and stop.
        Exception exception = (Exception)bundle.getSerializable("!EXCEPTION!");
        if(exception != null && canLaunchCrashReporterDialog()) {
            CrashException crashException = new CrashException(exception);
            crashException.setLocation(activity,null);
            crashException.appendExtraInfoFromArgument(bundle);
            CrashReporterDialogFragment.showCrashDialogFragment(CrashReporterDialog.class, crashException, activity, "crash");
            return;
        }

        try {
            onRestoreInstanceStateImpl(bundle);
            super.onRestoreInstanceState(bundle);
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);

            if(canLaunchCrashReporterDialog()) {
                CrashException crashException = new CrashException(e);
                crashException.setLocation(activity,null);
                crashException.appendExtraInfoFromArgument(bundle);
                CrashReporterDialogFragment.showCrashDialogFragment(CrashReporterDialog.class, crashException, activity, "crash");
            }
        }
    }

    // These are optional for subclasses to override.
    public void onBackPressedImpl() { super.onBackPressed(); } // Unlike in Activities, here we allow default behavior.
    protected void onCreateImpl(Bundle savedInstanceState) {}
    public void showImpl() {}
    public Bundle onSaveInstanceStateImpl(Bundle bundle) { return bundle; }
    public void onRestoreInstanceStateImpl(Bundle bundle) {}
}
