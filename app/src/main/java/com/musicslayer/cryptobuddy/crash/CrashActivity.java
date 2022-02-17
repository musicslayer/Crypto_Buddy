package com.musicslayer.cryptobuddy.crash;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.musicslayer.cryptobuddy.dialog.BaseDialog;
import com.musicslayer.cryptobuddy.dialog.BaseDialogFragment;
import com.musicslayer.cryptobuddy.dialog.CrashReporterDialog;
import com.musicslayer.cryptobuddy.dialog.CrashReporterDialogFragment;
import com.musicslayer.cryptobuddy.util.ThrowableUtil;

import java.util.ArrayList;

abstract public class CrashActivity extends AppCompatActivity {
    public ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this::onActivityResult);

    @Override
    final public void onBackPressed() {
        try {
            onBackPressedImpl();
        }
        catch(CrashBypassException e) {
            // Do nothing.
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);

            CrashException crashException = new CrashException(e);
            crashException.setLocationInfo(this, null);

            CrashReporterDialogFragment.showCrashDialogFragment(CrashReporterDialog.class, crashException, this, "crash");
        }
    }

    @Override
    final public void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            onCreateImpl(savedInstanceState);
        }
        catch(CrashBypassException e) {
            // Do nothing.
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);

            CrashException crashException = new CrashException(e);
            crashException.setLocationInfo(this, null);
            crashException.appendExtraInfoFromArgument(savedInstanceState);

            CrashReporterDialogFragment.showCrashDialogFragment(CrashReporterDialog.class, crashException, this, "crash");
        }
    }

    @Override
    final public void onResume() {
        try {
            super.onResume();
            onResumeImpl();
        }
        catch(CrashBypassException e) {
            // Do nothing.
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);

            CrashException crashException = new CrashException(e);
            crashException.setLocationInfo(this, null);

            CrashReporterDialogFragment.showCrashDialogFragment(CrashReporterDialog.class, crashException, this, "crash");
        }
    }

    // Create our own method to listen for activity results and pass through to dialogs.
    final public void onActivityResult(ActivityResult result) {
        try {
            onActivityResultImpl(result);
        }
        catch(CrashBypassException e) {
            // Do nothing.
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);

            CrashException crashException = new CrashException(e);
            crashException.setLocationInfo(this, null);
            crashException.appendExtraInfoFromArgument(result);

            CrashReporterDialogFragment.showCrashDialogFragment(CrashReporterDialog.class, crashException, this, "crash");
        }
    }

    @Override
    final public boolean onCreateOptionsMenu(Menu menu) {
        try {
            return onCreateOptionsMenuImpl(menu);
        }
        catch(CrashBypassException e) {
            // Do nothing.
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);

            CrashException crashException = new CrashException(e);
            crashException.setLocationInfo(this,null);
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
        catch(CrashBypassException e) {
            // Do nothing.
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);

            CrashException crashException = new CrashException(e);
            crashException.setLocationInfo(this,null);
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
        catch(CrashBypassException e) {
            // Do nothing.
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
            crashException.setLocationInfo(this,null);
            crashException.appendExtraInfoFromArgument(bundle);
            CrashReporterDialogFragment.showCrashDialogFragment(CrashReporterDialog.class, crashException, this, "crash");
            return;
        }

        try {
            onRestoreInstanceStateImpl(bundle);
            super.onRestoreInstanceState(bundle);
        }
        catch(CrashBypassException e) {
            // Do nothing.
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);

            CrashException crashException = new CrashException(e);
            crashException.setLocationInfo(this,null);
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

    // Call our own "onResume" method on the uppermost visible Dialog.
    public void onResumeImpl() {
        ArrayList<Dialog> baseDialogArrayList = BaseDialogFragment.getAllDialogs(this);
        if(!baseDialogArrayList.isEmpty()) {
            BaseDialog lastBaseDialog = (BaseDialog) baseDialogArrayList.get(baseDialogArrayList.size() - 1);
            lastBaseDialog.onResume();
        }
    }

    // Call our own "onActivityResult" method on the uppermost visible Dialog.
    public void onActivityResultImpl(ActivityResult result) {
        ArrayList<Dialog> baseDialogArrayList = BaseDialogFragment.getAllDialogs(this);
        if(!baseDialogArrayList.isEmpty()) {
            BaseDialog lastBaseDialog = (BaseDialog) baseDialogArrayList.get(baseDialogArrayList.size() - 1);
            lastBaseDialog.onActivityResult(result);
        }
    }
}
