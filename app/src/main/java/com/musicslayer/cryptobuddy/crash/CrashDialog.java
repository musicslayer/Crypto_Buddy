package com.musicslayer.cryptobuddy.crash;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.musicslayer.cryptobuddy.dialog.CrashReporterDialog;
import com.musicslayer.cryptobuddy.dialog.CrashReporterDialogFragment;
import com.musicslayer.cryptobuddy.util.ThrowableLogger;

abstract public class CrashDialog extends Dialog {
    private final AppCompatActivity activity;

    public CrashDialog(Activity activity) {
        super(activity);
        this.activity = (AppCompatActivity)activity;
    }

    @Override
    final public void onBackPressed() {
        try {
            onBackPressedImpl();
        }
        catch(Exception e) {
            ThrowableLogger.processThrowable(e);

            if(!(this instanceof CrashReporterDialog) && activity.getSupportFragmentManager().findFragmentByTag("crash") == null) {
                CrashException crashException = new CrashException(e);
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
            ThrowableLogger.processThrowable(e);

            if(!(this instanceof CrashReporterDialog) && activity.getSupportFragmentManager().findFragmentByTag("crash") == null) {
                CrashException crashException = new CrashException(e);
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
            ThrowableLogger.processThrowable(e);

            if(!(this instanceof CrashReporterDialog) && activity.getSupportFragmentManager().findFragmentByTag("crash") == null) {
                CrashException crashException = new CrashException(e);
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
            ThrowableLogger.processThrowable(e);

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
        if(exception != null) {
            CrashException crashException = new CrashException(exception);
            CrashReporterDialogFragment.showCrashDialogFragment(CrashReporterDialog.class, crashException, activity, "crash");
            return;
        }

        try {
            onRestoreInstanceStateImpl(bundle);
            super.onRestoreInstanceState(bundle);
        }
        catch(Exception e) {
            ThrowableLogger.processThrowable(e);

            CrashException crashException = new CrashException(e);
            crashException.appendExtraInfoFromArgument(bundle);

            CrashReporterDialogFragment.showCrashDialogFragment(CrashReporterDialog.class, crashException, activity, "crash");
        }
    }

    // These are optional for subclasses to override.
    public void onBackPressedImpl() { super.onBackPressed(); } // Unlike in Activities, here we allow default behavior.
    protected void onCreateImpl(Bundle savedInstanceState) {}
    public void showImpl() {}
    public Bundle onSaveInstanceStateImpl(Bundle bundle) { return bundle; }
    public void onRestoreInstanceStateImpl(Bundle bundle) {}
}
