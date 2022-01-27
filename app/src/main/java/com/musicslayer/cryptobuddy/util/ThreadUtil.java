package com.musicslayer.cryptobuddy.util;

import android.util.Log;

import java.util.Map;

public class ThreadUtil {
    public static String getCurrentThreadInfo() {
        return Thread.currentThread().toString();
    }

    public static String threadDump() {
        // Shows all threads' stack traces.
        StringBuilder s = new StringBuilder();

        Map<Thread, StackTraceElement[]> threadMap = Thread.getAllStackTraces();
        for(Thread thread : threadMap.keySet()){
            s.append("=================================").append("\n");
            s.append(thread.toString()).append("\n");

            StackTraceElement[] stackArray = threadMap.get(thread);

            if(stackArray != null) {
                for (StackTraceElement stack : stackArray){
                    s.append("-->").append(stack.toString()).append("\n");
                }
            }
            s.append("\n");
        }

        Log.e("Crypto Buddy Dump", s.toString());
        return s.toString();
    }
}
