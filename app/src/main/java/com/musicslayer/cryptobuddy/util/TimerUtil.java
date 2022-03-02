package com.musicslayer.cryptobuddy.util;

import android.os.CountDownTimer;

import java.util.ArrayList;
import java.util.HashMap;

public class TimerUtil  {
    public static HashMap<String, CountDownTimer> timerHashMap = new HashMap<>();

    public static void startTimer(String label, long millisInFuture, long countDownInterval, TimerUtilListener timerUtilListener) {
        // Stop any existing timer first.
        stopTimer(label);

        CountDownTimer timer = new CountDownTimer(millisInFuture, countDownInterval) {
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

        HashMapUtil.putValueInMap(timerHashMap, label, timer);
    }

    public static void stopTimer(String label) {
        CountDownTimer timer = HashMapUtil.getValueFromMap(timerHashMap, label);
        if(timer != null) {
            timer.cancel();
            timer = null;
            HashMapUtil.putValueInMap(timerHashMap, label, timer);
        }
    }

    public static void reset() {
        for(String label : new ArrayList<>(timerHashMap.keySet())) {
            stopTimer(label);
        }
    }

    abstract public static class TimerUtilListener {
        abstract public void onTickCallback(long millisUntilFinished);
        abstract public void onFinishCallback();
    }
}
