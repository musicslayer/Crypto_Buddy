package com.musicslayer.cryptobuddy.util;

import android.os.CountDownTimer;

public class TimerUtil  {
    public static CountDownTimer timer;

    public static void startTimer(long millisInFuture, long countDownInterval, TimerUtilListener timerUtilListener) {
        // Stop any existing timer first.
        stopTimer();

        timer = new CountDownTimer(millisInFuture, countDownInterval) {
            @Override
            public void onTick(long millisUntilFinished) {
                timerUtilListener.onTickCallback(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                timerUtilListener.onFinishCallback();
            }
        };

        timer.start();
    }

    public static void stopTimer() {
        if(timer != null) {
            timer.cancel();
        }
    }

    abstract public static class TimerUtilListener {
        abstract public void onTickCallback(long millisUntilFinished);
        abstract public void onFinishCallback();
    }
}
