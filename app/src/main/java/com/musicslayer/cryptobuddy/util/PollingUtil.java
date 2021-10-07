package com.musicslayer.cryptobuddy.util;

import com.musicslayer.cryptobuddy.dialog.ProgressDialogFragment;

import java.util.Date;

public class PollingUtil {
    private static final long DEFAULT_waitTime = 10000000;

    private final PollingUtil.PollingUtilListener pollingUtilListener;
    private final long waitTime;

    private PollingUtil(PollingUtil.PollingUtilListener pollingUtilListener, long waitTime) {
        this.pollingUtilListener = pollingUtilListener;
        this.waitTime = waitTime;
    }

    private void pollImpl() {
        // Poll for a condition to be true.
        long start = new Date().getTime();
        long now = new Date().getTime();

        while(now - start < waitTime) {
            if(pollingUtilListener.breakCondition(this)) { break; }
            now = new Date().getTime();
        }
    }

    public static void pollFor(PollingUtil.PollingUtilListener pollingUtilListener) {
        // Poll for a condition to be true, but only wait at most for the default time.
        // This means that when this method returns, the condition may not actually be true.
        pollFor(pollingUtilListener, DEFAULT_waitTime);
    }

    public static void pollFor(PollingUtil.PollingUtilListener pollingUtilListener, long waitTime) {
        // Poll for a condition to be true, but only wait at most for "waitTime".
        // This means that when this method returns, the condition may not actually be true.
        new PollingUtil(pollingUtilListener, waitTime).pollImpl();
    }

    public static void waitFor(long waitTime) {
        // Just wait for a fixed amount of time to elapse.
        // An input of zero or a negative number will result in virtually no wait.
        PollingUtil pollingUtil = new PollingUtil(new PollingUtil.PollingUtilListener() {
            @Override
            public boolean breakCondition(PollingUtil pollingUtil) {
                return false;
            }
        }, waitTime);
        pollingUtil.pollImpl();
    }

    abstract public static class PollingUtilListener {
        abstract public boolean breakCondition(PollingUtil pollingUtil);
    }
}
