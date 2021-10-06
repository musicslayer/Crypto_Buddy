package com.musicslayer.cryptobuddy.crash;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.musicslayer.cryptobuddy.dialog.CrashReporterDialog;
import com.musicslayer.cryptobuddy.dialog.CrashReporterDialogFragment;
import com.musicslayer.cryptobuddy.util.ThrowableUtil;

abstract public class CrashActivity extends AppCompatActivity {
    @Override
    final public void onBackPressed() {
        try {
            onBackPressedImpl();
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);

            CrashException crashException = new CrashException(e);

            CrashReporterDialogFragment.showCrashDialogFragment(CrashReporterDialog.class, crashException, this, "crash");
        }
    }

    @Override
    final public void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            onCreateImpl(savedInstanceState);
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);

            CrashException crashException = new CrashException(e);
            crashException.appendExtraInfoFromArgument(savedInstanceState);

            CrashReporterDialogFragment.showCrashDialogFragment(CrashReporterDialog.class, crashException, this, "crash");
        }
    }

    @Override
    final public boolean onCreateOptionsMenu(Menu menu) {
        try {
            return onCreateOptionsMenuImpl(menu);
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);

            CrashException crashException = new CrashException(e);
            crashException.appendExtraInfoFromArgument(menu);

            CrashReporterDialogFragment.showCrashDialogFragment(CrashReporterDialog.class, crashException, this, "crash");
        }

        return false;
    }

    @Override
    final public boolean onOptionsItemSelected(MenuItem item) {
        try {
            return onOptionsItemSelectedImpl(item);
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);

            CrashException crashException = new CrashException(e);
            crashException.appendExtraInfoFromArgument(item);

            CrashReporterDialogFragment.showCrashDialogFragment(CrashReporterDialog.class, crashException, this, "crash");
        }

        return false;
    }

    @Override
    final public void onSaveInstanceState(@NonNull Bundle bundle) {
        try {
            super.onSaveInstanceState(bundle);
            onSaveInstanceStateImpl(bundle);
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);

            // We cannot show the dialog here, so just save the exception.
            bundle.putSerializable("!EXCEPTION!", e);
        }
    }

    @Override
    final public void onRestoreInstanceState(Bundle bundle) {
        // If onSaveInstanceState had an exception, just show the crash reporter and stop.
        Exception exception = (Exception)bundle.getSerializable("!EXCEPTION!");
        if(exception != null) {
            CrashException crashException = new CrashException(exception);
            crashException.appendExtraInfoFromArgument(bundle);
            CrashReporterDialogFragment.showCrashDialogFragment(CrashReporterDialog.class, crashException, this, "crash");
            return;
        }

        try {
            onRestoreInstanceStateImpl(bundle);
            super.onRestoreInstanceState(bundle);
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);

            CrashException crashException = new CrashException(e);
            crashException.appendExtraInfoFromArgument(bundle);

            CrashReporterDialogFragment.showCrashDialogFragment(CrashReporterDialog.class, crashException, this, "crash");
        }
    }

    // These are optional for subclasses to override.
    public void onBackPressedImpl() {}
    public void onCreateImpl(Bundle savedInstanceState) {}
    public boolean onCreateOptionsMenuImpl(Menu menu) { return false; }
    public boolean onOptionsItemSelectedImpl(MenuItem item) { return false; }
    public void onSaveInstanceStateImpl(@NonNull Bundle bundle) {}
    public void onRestoreInstanceStateImpl(Bundle bundle) {}
}
