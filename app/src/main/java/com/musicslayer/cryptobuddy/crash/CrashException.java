package com.musicslayer.cryptobuddy.crash;

import androidx.annotation.NonNull;

import com.musicslayer.cryptobuddy.util.ThrowableLogger;

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
            s.append("No Extra Info:\n\n");
        }
        else {
            s.append("Extra Info:\n").append(extraInfo).append("\n");
        }

        s.append(ThrowableLogger.getThrowableText(originalException));
        return s.toString();
    }

    public void appendExtraInfoFromArgument(Object obj) {
        // Take any object and try to append it's string information.
        try {
            this.extraInfoStringBuilder.append(obj.toString()).append("\n");
        }
        catch(Exception e) {
            ThrowableLogger.processThrowable(e);
            this.extraInfoStringBuilder.append("?\n");
        }
    }
}
