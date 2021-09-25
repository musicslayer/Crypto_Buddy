package com.musicslayer.cryptobuddy.util;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

import static android.content.ClipDescription.MIMETYPE_TEXT_PLAIN;

public class Clipboard {
    public static void copy(Context context, String label, String text) {
        Toast.showToast("copy");

        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setPrimaryClip(ClipData.newPlainText(label, text));
    }

    public static CharSequence paste(Context context) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        boolean hasText = clipboard.hasPrimaryClip() && clipboard.getPrimaryClipDescription().hasMimeType(MIMETYPE_TEXT_PLAIN) && !"".contentEquals(clipboard.getPrimaryClip().getItemAt(0).getText());

        if(hasText) {
            Toast.showToast("paste");
            return clipboard.getPrimaryClip().getItemAt(0).getText();
        }
        else {
            Toast.showToast("no_paste");
            return "";
        }
    }
}
