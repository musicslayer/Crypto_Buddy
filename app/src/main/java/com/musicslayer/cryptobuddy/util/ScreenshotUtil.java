package com.musicslayer.cryptobuddy.util;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.View;

import java.io.File;
import java.io.FileOutputStream;

public class ScreenshotUtil {
    public static File writeScreenshotFile(Activity activity) {
        // Captures the view of the Activity (without dialogs or anything else that is on top of it) and writes it to a file.
        // This only includes what is currently visible on the screen, not offscreen content (for example, the rest of a ScrollView).
        File file;
        try {
            file = File.createTempFile("CryptoBuddy_Screenshot", ".bmp", activity.getCacheDir());

            View view = activity.getWindow().getDecorView().getRootView();
            Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            view.draw(canvas);

            FileOutputStream outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);

            // This class may be used by CrashReporterDialog, so just return null instead of throwing something.
            file = null;
        }

        return file;
    }
}
