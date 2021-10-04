package com.musicslayer.cryptobuddy.dialog;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;

public class ProgressDialogFragment extends BaseDialogFragment {
    public static ProgressDialogFragment newInstance(Class<?> clazz, Object... args) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("class", clazz);
        bundle.putSerializable("args", args);

        ProgressDialogFragment fragment = new ProgressDialogFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onShow(@NonNull DialogInterface dialog) {
        // Uses both onShow and onDismiss listeners here.
        ProgressDialog currentProgressDialog = (ProgressDialog)dialog;

        new Thread(new Runnable() {
            @Override
            public void run() {
                if(SL != null && !currentProgressDialog.isCancelled) {
                    SL.onShow(currentProgressDialog);
                }

                // Only the onDismiss listener should update any UI elements.
                currentProgressDialog.activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(DL != null && !currentProgressDialog.isCancelled) {
                            DL.onDismiss(currentProgressDialog);
                        }

                        currentProgressDialog.dismiss();
                    }
                });
            }
        }).start();
    }

    @Override
    public void doDismiss(@NonNull DialogInterface dialog) {
        // Unlike in the superclass, here we do not want to use the onDismiss listener.
        // Instead, any dialog that gets dismissed needs to be cancelled.
        ((ProgressDialog)dialog).isCancelled = true;
    }
}
