package com.musicslayer.cryptobuddy.util;

import android.util.Log;

public class Exception {
    public static void processException(java.lang.Exception e) {
        Log.e("Crypto Buddy ERROR", getExceptionText(e));
    }

    public static String getExceptionText(java.lang.Exception e) {
        StringBuilder s = new StringBuilder();

        s.append(e.getMessage());

        StackTraceElement[] stackArray = e.getStackTrace();
        for(StackTraceElement stack : stackArray) {
            s.append("\n--> ").append(stack.toString());
        }

        return s.toString();
    }
}
