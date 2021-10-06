package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.musicslayer.cryptobuddy.crash.CrashException;
import com.musicslayer.cryptobuddy.util.Reflect;
import com.musicslayer.cryptobuddy.util.ThrowableLogger;

public class CrashReporterDialogFragment extends BaseDialogFragment {
    // We only want to deal with an app's first crash, so use this flag to prevent multiple CrashReporterDialogs from showing.
    final static boolean[] LAUNCHED = new boolean[1];

    public static void showCrashDialogFragment(Class<?> clazz, CrashException crashException, Activity activity, String tag) {
        try {
            if(!LAUNCHED[0]) {
                // Set this first, because the call to "show" may throw even though it will eventually succeed.
                LAUNCHED[0] = true;
                CrashReporterDialogFragment.newInstance(clazz, crashException).show(activity, tag);
            }
        }
        catch(Exception e) {
            // Even in cases where CrashReporterDialog would successfully show, sometimes exceptions are thrown which we can ignore.
            ThrowableLogger.processThrowable(e);
        }
    }

    public static CrashReporterDialogFragment newInstance(Class<?> clazz, CrashException crashException) {
        // Always use one argument.
        Bundle bundle = new Bundle();
        bundle.putSerializable("class", clazz);
        bundle.putSerializable("crash_exception", crashException);

        CrashReporterDialogFragment fragment = new CrashReporterDialogFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Object object = null;

        Bundle bundle = getArguments();
        if(bundle != null) {
            Class<?> clazz = (Class<?>)bundle.getSerializable("class");
            CrashException crashException = (CrashException)bundle.getSerializable("crash_exception");

            object = Reflect.constructCrashReporterDialogInstance(clazz, getActivity(), crashException);
            ((Dialog)object).setOnShowListener(this);
        }

        return (Dialog)object;
    }

    // CrashReporterDialog cannot execute any listeners.
    @Override
    public void onShow(@NonNull DialogInterface dialog) {
    }

    @Override
    public void doDismiss(@NonNull DialogInterface dialog) {
    }
}
