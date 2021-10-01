package com.musicslayer.cryptobuddy.util;

import android.util.Log;

public class ExceptionLogger {
    public static void processException(Exception e) {
        Log.e("Crypto Buddy ERROR", getExceptionText(e));
    }

    public static String getExceptionText(Exception e) {
        StringBuilder s = new StringBuilder();

        s.append(e.getMessage());

        StackTraceElement[] stackArray = e.getStackTrace();
        for(StackTraceElement stack : stackArray) {
            s.append("\n--> ").append(stack.toString());
        }

        return s.toString();
    }
}
