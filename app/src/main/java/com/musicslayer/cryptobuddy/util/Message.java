package com.musicslayer.cryptobuddy.util;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;

public class Message {
    public static void sendEmail(Activity activity, String toText, String subjectText, String bodyText) {
        String encodedToText = Uri.encode(toText);
        String encodedSubjectText = Uri.encode(subjectText);
        String encodedBodyText = Uri.encode(bodyText);
        final Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:?to=" + encodedToText + "&subject=" + encodedSubjectText + "&body=" + encodedBodyText));

        ComponentName emailApp = emailIntent.resolveActivity(activity.getPackageManager());
        ComponentName unsupportedAction = ComponentName.unflattenFromString("com.android.fallback/.Fallback");
        if(emailApp != null && !emailApp.equals(unsupportedAction)) {
            activity.startActivity(emailIntent);
        }
        else {
            Toast.showToast("email");
        }
    }

    public static void sendSMS(Activity activity, String bodyText) {
        final Intent smsIntent = new Intent(Intent.ACTION_VIEW);
        smsIntent.putExtra("sms_body", bodyText);
        smsIntent.setType("vnd.android-dir/mms-sms");

        ComponentName smsApp = smsIntent.resolveActivity(activity.getPackageManager());
        ComponentName unsupportedAction = ComponentName.unflattenFromString("com.android.fallback/.Fallback");
        if(smsApp != null && !smsApp.equals(unsupportedAction)) {
            activity.startActivity(smsIntent);
        }
        else {
            Toast.showToast("sms");
        }
    }
}
