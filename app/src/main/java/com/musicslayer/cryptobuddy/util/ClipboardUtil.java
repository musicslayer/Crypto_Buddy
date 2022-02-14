package com.musicslayer.cryptobuddy.util;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

import static android.content.ClipDescription.MIMETYPE_TEXT_PLAIN;

public class ClipboardUtil {
    public static void copy(Context context, String label, String text) {
        ToastUtil.showToast(context,"copy");

        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setPrimaryClip(ClipData.newPlainText(label, text));
    }

    public static CharSequence paste(Context context) {
        // TODO Separate messages for empty text and not text.
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        boolean hasText = clipboard.hasPrimaryClip() && clipboard.getPrimaryClipDescription().hasMimeType(MIMETYPE_TEXT_PLAIN) && !"".contentEquals(clipboard.getPrimaryClip().getItemAt(0).getText());

        if(hasText) {
            ToastUtil.showToast(context,"paste");
            return clipboard.getPrimaryClip().getItemAt(0).getText();
        }
        else {
            ToastUtil.showToast(context,"no_paste");
            return "";
        }
    }

    public static CharSequence getText(Context context) {
        // Get Clipboard text without showing any messages.
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        boolean hasText = clipboard.hasPrimaryClip() && clipboard.getPrimaryClipDescription().hasMimeType(MIMETYPE_TEXT_PLAIN);

        if(!hasText) {
            return null;
        }
        else {
            return clipboard.getPrimaryClip().getItemAt(0).getText();
        }
    }
}
