package com.musicslayer.cryptobuddy.crash;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.AdapterView;

import com.musicslayer.cryptobuddy.dialog.CrashReporterDialog;
import com.musicslayer.cryptobuddy.dialog.CrashReporterDialogFragment;
import com.musicslayer.cryptobuddy.util.ContextUtil;
import com.musicslayer.cryptobuddy.util.ThrowableUtil;

abstract public class CrashOnItemSelectedListener implements AdapterView.OnItemSelectedListener {
    abstract public void onNothingSelectedImpl(AdapterView<?> parent);
    abstract public void onItemSelectedImpl(AdapterView<?> parent, View view, int pos, long id);

    public Activity activity;

    public CrashOnItemSelectedListener(Context context) {
        this.activity = ContextUtil.getActivity(context);
    }

    @Override
    final public void onNothingSelected(AdapterView<?> parent) {
        try {
            onNothingSelectedImpl(parent);
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);

            CrashException crashException = new CrashException(e);
            crashException.appendExtraInfoFromArgument(parent);

            CrashReporterDialogFragment.showCrashDialogFragment(CrashReporterDialog.class, crashException, activity, "crash");
        }
    }

    @Override
    final public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        try {
            onItemSelectedImpl(parent, view, pos, id);
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);

            CrashException crashException = new CrashException(e);
            crashException.appendExtraInfoFromArgument(parent);
            crashException.appendExtraInfoFromArgument(view);
            crashException.appendExtraInfoFromArgument(pos);
            crashException.appendExtraInfoFromArgument(id);

            CrashReporterDialogFragment.showCrashDialogFragment(CrashReporterDialog.class, crashException, activity, "crash");
        }
    }
}
