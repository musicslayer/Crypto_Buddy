package com.musicslayer.cryptobuddy.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.musicslayer.cryptobuddy.crash.CrashBypassException;
import com.musicslayer.cryptobuddy.settings.setting.ProgressDisplaySetting;
import com.musicslayer.cryptobuddy.util.PollingUtil;
import com.musicslayer.cryptobuddy.util.TimerUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class ProgressDialogFragment extends BaseDialogFragment {
    public static final long MAX_TIME = 3600000L; // 60 minutes
    public static final long UPDATE_TIME = 1000L; // 1 second

    public final static String START = "!START!";
    public final static String IN_PROGRESS = "!IN_PROGRESS!";
    public final static String CANCELLED = "!CANCELLED!";
    public final static String DONE = "!DONE!";

    public final static String[] stored_status = new String[1];
    public final static String[] stored_value = new String[1];

    public final static String[] progress_title = new String[1];
    public final static String[] progress_subtitle = new String[1];
    public final static String[] progress_display = new String[1];

    // Global switch that determines if threads created here should proceed on.
    public final static boolean[] allowThreads = new boolean[1];

    public static ProgressDialogFragment newInstance(Class<?> clazz, Object... args) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("class", clazz);
        bundle.putSerializable("args", args);

        ProgressDialogFragment fragment = new ProgressDialogFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    public static void checkForInterrupt() {
        // If ProgressDialog was cancelled, throw an error so the thread can stop running.
        // Right now, this implicitly requires the caller to use ProgressDialog.
        if(!allowThreads[0]) {
            throw new CrashBypassException();
        }
    }

    public static void updateProgressTitle(String s) {
        ProgressDialogFragment.progress_title[0] = s;
        ProgressDialogFragment.progress_subtitle[0] = null;
        ProgressDialogFragment.progress_display[0] = null;
    }

    public static void updateProgressSubtitle(String s) {
        ProgressDialogFragment.progress_subtitle[0] = s;
        ProgressDialogFragment.progress_display[0] = null;
    }

    public static void updateProgressDisplay(String s) {
        ProgressDialogFragment.progress_display[0] = s;
    }

    public static void reportProgress(int current, int total, String postString) {
        String totalString = total == -1 ? "?" : Integer.toString(total);
        String percentageString = total == -1 ? "?" : new BigDecimal(current).multiply(new BigDecimal(100)).divide(new BigDecimal(total), 0, RoundingMode.HALF_UP).toPlainString();

        String progressSetting = ProgressDisplaySetting.value;
        if("combo".equals(progressSetting)) {
            updateProgressDisplay(current + " / " + totalString + " (" + percentageString + "%) " + postString);
        }
        else if("percentage".equals(progressSetting)) {
            updateProgressDisplay(percentageString + "% " + postString);
        }
        else if("total".equals(progressSetting)) {
            updateProgressDisplay(current + " / " + totalString + " " + postString);
        }
    }

    @Override
    public void show(Context context, String tag) {
        // Clear the stored state before every new ProgressDialog showing.
        ProgressDialogFragment.clearAll();
        allowThreads[0] = true;

        // Set initial title and display.
        ProgressDialogFragment.progress_title[0] = null;
        ProgressDialogFragment.progress_subtitle[0] = null;
        ProgressDialogFragment.progress_display[0] = null;

        super.show(context, tag);
    }

    @Override
    public void onShow(@NonNull DialogInterface dialog) {
        // Uses both onShow and onDismiss listeners here.
        ProgressDialog currentProgressDialog = (ProgressDialog)dialog;

        // Create timer to periodically update the layout.
        // This will cancel any prior timer, and make sure callbacks fire on the current dialog.
        // Note that it's OK to keep starting new timers since the actual information will be in progress_title/progress_display.
        // This timer only tells the text to keep checking for new info.
        TimerUtil.startTimer(MAX_TIME, UPDATE_TIME, new TimerUtil.TimerUtilListener() {
            @Override
            public void onTickCallback(long millisUntilFinished) {
                currentProgressDialog.activity.runOnUiThread(() -> {
                    currentProgressDialog.updateLayout();
                });
            }

            @Override
            public void onFinishCallback() {}
        });

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
    public void doDismiss(@NonNull DialogInterface dialog) {
        // Stop the progress update timer.
        TimerUtil.stopTimer();

        // Unlike in the superclass, here we do not want to use the onDismiss listener.
        // Instead, any dialog that gets dismissed needs to be cancelled.
        ((ProgressDialog)dialog).isCancelled = true;

        // When the operation is finished, or if we deliberately stopped the operation while it was still running,
        // we must signal to the threads we need them to stop so they don't keep using up resources.
        if(ProgressDialogFragment.isCancelled() || ProgressDialogFragment.isDone()) {
            allowThreads[0] = false;
        }
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
