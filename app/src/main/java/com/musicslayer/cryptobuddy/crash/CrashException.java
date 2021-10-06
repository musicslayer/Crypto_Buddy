package com.musicslayer.cryptobuddy.crash;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;

import androidx.annotation.NonNull;

import com.musicslayer.cryptobuddy.dialog.BaseDialogFragment;
import com.musicslayer.cryptobuddy.util.ThrowableUtil;

import java.util.ArrayList;

// An Exception wrapper used in Crash classes. We can wrap other exceptions and add in useful information.
public class CrashException extends RuntimeException {
    // Properties that help indicate where the crash originally occurred.
    public Activity originalActivity;
    public ArrayList<Dialog> originalDialogArrayList;
    public View originalView;

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

        if(originalActivity == null && originalDialogArrayList == null && originalView == null) {
            s.append("No Location Info:\n\n");
        }
        else {
            s.append("Location Info:\n");

            try {
                if(originalActivity != null) {
                    s.append(originalActivity.getClass().getSimpleName()).append("\n");
                }
                if(originalDialogArrayList != null) {
                    for(Dialog originalDialog : originalDialogArrayList) {
                        s.append(originalDialog.getClass().getSimpleName()).append("\n");
                    }
                }
                if(originalView != null) {
                    s.append(originalView.getClass().getSimpleName()).append("\n");
                }
            }
            catch(Exception e) {
                ThrowableUtil.processThrowable(e);
                s.append("?\n");
            }

            s.append("\n");
        }


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

    public void setLocation(Activity originalActivity, View originalView) {
        // null is passed in if the information isn't available.
        this.originalActivity = originalActivity;
        if(originalActivity != null) {
            this.originalDialogArrayList = BaseDialogFragment.getAllDialogs(originalActivity);
        }
        else {
            this.originalDialogArrayList = null;
        }
        this.originalView = originalView;
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
        // Use a trick to throw any Throwable without having to declare or catch anything.
        forceThrow(originalException);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> void forceThrow(Throwable throwable) throws T {
        throw (T)throwable;
    }
}
