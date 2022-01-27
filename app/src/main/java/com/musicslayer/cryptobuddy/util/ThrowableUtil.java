package com.musicslayer.cryptobuddy.util;

import android.util.Log;

public class ThrowableUtil {
    public static void processThrowable(Throwable e) {
        try {
            String threadText = ThreadUtil.getCurrentThreadInfo();
            Log.e("Crypto Buddy ERROR", threadText + "\n" + getThrowableText(e));
        }
        catch(Exception ignored) {
        }
    }

    public static String getThrowableText(Throwable e) {
        try {
            StringBuilder s = new StringBuilder();

            s.append(e.toString());

            StackTraceElement[] stackArray = e.getStackTrace();
            for(StackTraceElement stack : stackArray) {
                s.append("\n--> ").append(stack.toString());
            }

            // Process causes recursively.
            Throwable cause = e.getCause();
            if(cause != null) {
                s.append("\n\nCaused By:\n").append(getThrowableText(cause));
            }

            return s.toString();
        }
        catch(Exception ignored) {
            return "?";
        }
    }
}
