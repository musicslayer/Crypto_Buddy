package com.musicslayer.cryptobuddy.crash;

import androidx.annotation.NonNull;

import com.musicslayer.cryptobuddy.util.ThrowableUtil;

// An Exception wrapper used in Crash classes. We can wrap other exceptions and add in useful information.
public class CrashException extends RuntimeException {
    // Extra info about the exception, such as the objects involved.
    public StringBuilder extraInfoStringBuilder = new StringBuilder();

    // The original exception that caused the crash.
    public Exception originalException;

    public CrashException(Exception originalException) {
        this.originalException = originalException;
    }

    @NonNull
    public String toString() {
        StringBuilder s = new StringBuilder();
        String extraInfo = extraInfoStringBuilder.toString();

        if(extraInfo.isEmpty()) {
            s.append("No Extra Info.\n\n");
        }
        else {
            s.append("Extra Info:\n").append(extraInfo).append("\n");
        }

        s.append(ThrowableUtil.getThrowableText(originalException));
        return s.toString();
    }

    public void appendExtraInfoFromArgument(Object obj) {
        // Take any object and try to append it's string information.
        try {
            if(obj == null) {
                this.extraInfoStringBuilder.append("null").append("\n");
            }
            else {
                this.extraInfoStringBuilder.append(obj.toString()).append("\n");
            }
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            this.extraInfoStringBuilder.append("?\n");
        }
    }

    public void throwOriginalException() {
        // Use a trick to throw without having to declare or catch anything.
        forceThrow(originalException);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> void forceThrow(Throwable exception) throws T {
        throw (T)exception;
    }
}
