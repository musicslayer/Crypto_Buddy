package com.musicslayer.cryptobuddy.util;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Rect;

public class WindowUtil {
    public static int[] getDimensions(Activity activity) {
        Rect displayRectangle = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(displayRectangle);

        int w = displayRectangle.width();
        int h = displayRectangle.height();

        int W;
        int H;

        // In landscape the larger value is the width, and in portrait the larger value is the height.
        // We must do this because the rectangle may be using a stale orientation.
        if(activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if(w > h) {
                W = w;
                H = h;
            }
            else {
                W = h;
                H = w;
            }
        }
        else {
            if(w > h) {
                H = w;
                W = h;
            }
            else {
                H = h;
                W = w;
            }
        }

        return new int[]{W, H};
    }
}
