package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.musicslayer.cryptobuddy.util.Reflect;

public class CrashDialogFragment extends BaseDialogFragment {
    public static void showCrashDialogFragment(Class<?> clazz, Exception e, Activity activity, String tag) {
        // Even in cases where CrashDialog would successfully show, sometimes exceptions are thrown which we can ignore.
        try {
            CrashDialogFragment.newInstance(clazz, e).show(activity, tag);
        }
        catch(Exception ignored) {
        }
    }

    public static CrashDialogFragment newInstance(Class<?> clazz, Exception e) {
        // Always use one argument.
        Bundle bundle = new Bundle();
        bundle.putSerializable("class", clazz);
        bundle.putSerializable("exception", e);

        CrashDialogFragment fragment = new CrashDialogFragment();
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
            Exception e = (Exception)bundle.getSerializable("exception");

            object = Reflect.constructCrashDialogInstance(clazz, getActivity(), e);
            ((Dialog)object).setOnShowListener(this);
        }

        return (Dialog)object;
    }

    // CrashDialog cannot execute any listeners.
    @Override
    public void onShow(@NonNull DialogInterface dialog) {
    }

    @Override
    public void doDismiss(@NonNull DialogInterface dialog) {
    }
}
