package com.musicslayer.cryptobuddy.util;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;

public class WebUtil {
    public static void launchURL(Activity activity, String url) {
        Intent webIntent = new Intent(Intent.ACTION_VIEW);
        webIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY); // Used so that we can close the web browser window programmatically.
        webIntent.setData(Uri.parse(url));

        ComponentName webApp = webIntent.resolveActivity(activity.getPackageManager());
        ComponentName unsupportedAction = ComponentName.unflattenFromString("com.android.fallback/.Fallback");
        if(webApp != null && !webApp.equals(unsupportedAction)) {
            activity.startActivity(webIntent);
        }
        else {
            ToastUtil.showToast(activity,"web_browser");
        }
    }
}
