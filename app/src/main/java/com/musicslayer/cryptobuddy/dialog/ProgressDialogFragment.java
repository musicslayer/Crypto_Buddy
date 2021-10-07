package com.musicslayer.cryptobuddy.dialog;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
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

        new Thread(() -> {
            if(SL != null && !currentProgressDialog.isCancelled) {
                SL.onShow(currentProgressDialog);
            }

            // Only the onDismiss listener should update any UI elements.
            currentProgressDialog.activity.runOnUiThread(() -> {
                if(DL != null && !currentProgressDialog.isCancelled) {
                    DL.onDismiss(currentProgressDialog);
                }

                currentProgressDialog.dismiss();
            });
        }).start();
    }

    @Override
    public void doDismiss(@NonNull DialogInterface dialog) {
        // Unlike in the superclass, here we do not want to use the onDismiss listener.
        // Instead, any dialog that gets dismissed needs to be cancelled.
        ((ProgressDialog)dialog).isCancelled = true;
    }

    // These methods below enable us to start the progress function once, and then get the result shown even if the activity/dialog is recreated.

    public static String getProgressValue(Context context) {
        SharedPreferences settings = context.getSharedPreferences("progress_data", MODE_PRIVATE);
        return settings.getString("progress_value", "!DEFAULT!");
    }

    public static void setProgressValueOnce(Context context, String value) {
        // Only set if we haven't started.
        String existingValue = getProgressValue(context);
        if(!("!DEFAULT!".equals(existingValue) || "!STARTED!".equals(existingValue))) { return; }

        SharedPreferences settings = context.getSharedPreferences("progress_data", MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.putString("progress_value", value);
        editor.apply();
    }

    public static void clearProgressValue(Context context) {
        SharedPreferences settings = context.getSharedPreferences("progress_data", MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.clear();
        editor.apply();
    }
}
