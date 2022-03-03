package com.musicslayer.cryptobuddy.util;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.View;

import com.musicslayer.cryptobuddy.activity.BaseActivity;
import com.musicslayer.cryptobuddy.app.App;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class ScreenshotUtil {
    public static File writeScreenshotFile(Activity activity) {
        // Captures the view of the Activity (without dialogs or anything else that is on top of it) and writes it to a file.
        // This only includes what is currently visible on the screen, not offscreen content (for example, the rest of a ScrollView).
        // This also does not include charts or other SurfaceViews.
        File file;
        FileOutputStream outputStream = null;

        try {
            file = File.createTempFile("CryptoBuddy_ScreenshotFile_", ".bmp", new File(App.cacheDir));

            View view = activity.getWindow().getDecorView().getRootView();
            Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            view.draw(canvas);

            outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            StreamUtil.safeFlushAndClose(outputStream);
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            StreamUtil.safeFlushAndClose(outputStream);

            // This class may be used by CrashReporterDialog, so just return null instead of throwing something.
            file = null;
        }

        return file;
    }

    public static File writeSurfaceFile(Activity activity) {
        // Alternate function to deal with SurfaceViews.
        // Most activities do not have any, but Chart activities need to use this.
        File file;
        FileOutputStream outputStream = null;

        try {
            ArrayList<Bitmap> bitmapArrayList = ((BaseActivity)activity).getSurfaceBitmaps();
            if(bitmapArrayList == null || bitmapArrayList.isEmpty()) {
                // We don't need this file.
                return null;
            }

            // Merge bitmaps so we can have one file.
            Bitmap bitmap = mergeBitmaps(bitmapArrayList);
            file = File.createTempFile("CryptoBuddy_SurfaceFile_", ".bmp", new File(App.cacheDir));

            outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            StreamUtil.safeFlushAndClose(outputStream);
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            StreamUtil.safeFlushAndClose(outputStream);

            // This class may be used by CrashReporterDialog, so just return null instead of throwing something.
            file = null;
        }

        return file;
    }

    private static Bitmap mergeBitmaps(ArrayList<Bitmap> bitmapArrayList) {
        // Combine all the bitmaps in a column.
        // The merged width will be the max width of any bitmap.
        // The merged height will be the sum of all bitmap heights.
        int width = 0;
        int height = 0;
        for(Bitmap bitmap : bitmapArrayList) {
            width = Math.max(width, bitmap.getWidth());
            height += bitmap.getHeight();
        }

        Bitmap merged = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(merged);

        int top = 0;
        for(Bitmap bitmap : bitmapArrayList) {
            //top = (i == 0 ? 0 : top+bitmap.get(i).getHeight());
            canvas.drawBitmap(bitmap, 0f, top, null);
            top += bitmap.getHeight();
        }

        return merged;
    }
}
