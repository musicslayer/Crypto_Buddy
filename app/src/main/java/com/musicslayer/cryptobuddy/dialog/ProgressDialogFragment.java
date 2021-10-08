package com.musicslayer.cryptobuddy.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.musicslayer.cryptobuddy.util.PollingUtil;

public class ProgressDialogFragment extends BaseDialogFragment {
    public final static String START = "!START!";
    public final static String IN_PROGRESS = "!IN_PROGRESS!";
    public final static String CANCELLED = "!CANCELLED!";
    public final static String DONE = "!DONE!";

    public final static String[] stored_status = new String[1];
    public final static String[] stored_value = new String[1];

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
                if(ProgressDialogFragment.isStart()) {
                    // This is the first call, so actually do the work.
                    ProgressDialogFragment.setInProgress();

                    SL.onShow(currentProgressDialog);

                    if(!ProgressDialogFragment.isCancelled()) {
                        ProgressDialogFragment.setDone();
                    }
                }
                else {
                    // We are already in progress. Just wait for the result.
                    PollingUtil.pollFor(new PollingUtil.PollingUtilListener() {
                        @Override
                        public boolean breakCondition(PollingUtil pollingUtil) {
                            return !ProgressDialogFragment.isInProgress();
                        }
                    });
                }
            }

            // Only the onDismiss listener should update any UI elements.
            currentProgressDialog.activity.runOnUiThread(() -> {
                if(DL != null && !currentProgressDialog.isCancelled && ProgressDialogFragment.isDone()) {
                    DL.onDismiss(currentProgressDialog);
                }

                currentProgressDialog.dismiss();
            });
        }).start();
    }

    @Override
    public void show(Context context, String tag) {
        // Clear the stored state before every new ProgressDialog showing.
        ProgressDialogFragment.clearAll();
        super.show(context, tag);
    }

    @Override
    public void doDismiss(@NonNull DialogInterface dialog) {
        // Unlike in the superclass, here we do not want to use the onDismiss listener.
        // Instead, any dialog that gets dismissed needs to be cancelled.
        ((ProgressDialog)dialog).isCancelled = true;
    }

    // These methods below enable us to start the progress function once, and then get the result shown even if the activity/dialog is recreated.

    public static String getValue() {
        return stored_value[0];
    }

    public static void setValue(String value) {
        stored_value[0] = value;
    }

    public static void clearAll() {
        // Set these to their defaults.
        stored_status[0] = START;
        stored_value[0] = null;
    }

    public static String getStatus() {
        return stored_status[0];
    }

    public static void setInProgress() {
        stored_status[0] = IN_PROGRESS;
    }

    public static void setCancelled() {
        stored_status[0] = CANCELLED;
    }

    public static void setDone() {
        stored_status[0] = DONE;
    }

    public static boolean isStart() {
        return START.equals(ProgressDialogFragment.getStatus());
    }

    public static boolean isInProgress() {
        return IN_PROGRESS.equals(ProgressDialogFragment.getStatus());
    }

    public static boolean isCancelled() {
        return CANCELLED.equals(ProgressDialogFragment.getStatus());
    }

    public static boolean isDone() {
        return DONE.equals(ProgressDialogFragment.getStatus());
    }
}
