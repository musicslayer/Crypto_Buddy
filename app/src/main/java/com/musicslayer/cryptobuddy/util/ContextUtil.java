package com.musicslayer.cryptobuddy.util;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;

public class ContextUtil {
    public static Activity getActivity(Context context) {
        // Returns the Activity associated with the Context, or errors if we cannot.
        while (!(context instanceof Activity) && context instanceof ContextWrapper) {
            context = ((ContextWrapper) context).getBaseContext();
        }

        if(context instanceof Activity) {
            return ((Activity)context);
        }
        else {
            throw new IllegalStateException("Could not get activity: " + context.toString());
        }
    }
}
