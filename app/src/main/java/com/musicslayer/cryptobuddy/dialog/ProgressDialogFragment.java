package com.musicslayer.cryptobuddy.dialog;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.musicslayer.cryptobuddy.util.PollingUtil;

public class ProgressDialogFragment extends BaseDialogFragment {
    public final static String START = "!START!";
    public final static String IN_PROGRESS = "!IN_PROGRESS!";
    public final static String CANCELLED = "!CANCELLED!";
    public final static String DONE = "!DONE!";

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
                if(ProgressDialogFragment.isStart(currentProgressDialog.activity)) {
                    // This is the first call, so actually do the work.
                    ProgressDialogFragment.setInProgress(currentProgressDialog.activity);

                    SL.onShow(currentProgressDialog);

                    if(!ProgressDialogFragment.isCancelled(currentProgressDialog.activity)) {
                        ProgressDialogFragment.setDone(currentProgressDialog.activity);
                    }
                }
                else {
                    // We are already in progress. Just wait for the result.
                    PollingUtil.pollFor(new PollingUtil.PollingUtilListener() {
                        @Override
                        public boolean breakCondition(PollingUtil pollingUtil) {
                            return !ProgressDialogFragment.isInProgress(currentProgressDialog.activity);
                        }
                    });
                }
            }

            // Only the onDismiss listener should update any UI elements.
            currentProgressDialog.activity.runOnUiThread(() -> {
                if(DL != null && !currentProgressDialog.isCancelled && ProgressDialogFragment.isDone(currentProgressDialog.activity)) {
                    DL.onDismiss(currentProgressDialog);
                }

                currentProgressDialog.dismiss();
            });
        }).start();
    }

    @Override
    public void show(Context context, String tag) {
        ProgressDialogFragment.clearAll(context);
        super.show(context, tag);
    }

    @Override
    public void doDismiss(@NonNull DialogInterface dialog) {
        // Unlike in the superclass, here we do not want to use the onDismiss listener.
        // Instead, any dialog that gets dismissed needs to be cancelled.
        ((ProgressDialog)dialog).isCancelled = true;
    }

    // These methods below enable us to start the progress function once, and then get the result shown even if the activity/dialog is recreated.

    public static String getValue(Context context) {
        SharedPreferences settings = context.getSharedPreferences("progress_data", MODE_PRIVATE);
        return settings.getString("progress_value", null);
    }

    public static void setValue(Context context, String value) {
        SharedPreferences settings = context.getSharedPreferences("progress_data", MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.putString("progress_value", value);
        editor.apply();
    }

    public static void clearAll(Context context) {
        // This is equivalent to setting the status to START and the value to null.
        SharedPreferences settings = context.getSharedPreferences("progress_data", MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.clear();
        editor.apply();
    }

    public static String getStatus(Context context) {
        SharedPreferences settings = context.getSharedPreferences("progress_data", MODE_PRIVATE);
        return settings.getString("progress_status", START);
    }

    public static void setInProgress(Context context) {
        SharedPreferences settings = context.getSharedPreferences("progress_data", MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.putString("progress_status", IN_PROGRESS);
        editor.apply();
    }

    public static void setCancelled(Context context) {
        SharedPreferences settings = context.getSharedPreferences("progress_data", MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.putString("progress_status", CANCELLED);
        editor.apply();
    }

    public static void setDone(Context context) {
        SharedPreferences settings = context.getSharedPreferences("progress_data", MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.putString("progress_status", DONE);
        editor.apply();
    }

    public static boolean isStart(Context context) {
        return START.equals(ProgressDialogFragment.getStatus(context));
    }

    public static boolean isInProgress(Context context) {
        return IN_PROGRESS.equals(ProgressDialogFragment.getStatus(context));
    }

    public static boolean isCancelled(Context context) {
        return CANCELLED.equals(ProgressDialogFragment.getStatus(context));
    }

    public static boolean isDone(Context context) {
        return DONE.equals(ProgressDialogFragment.getStatus(context));
    }
}
