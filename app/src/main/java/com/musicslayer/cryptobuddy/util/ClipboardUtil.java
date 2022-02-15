package com.musicslayer.cryptobuddy.util;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

import static android.content.ClipDescription.MIMETYPE_TEXT_PLAIN;

public class ClipboardUtil {
    public static void copy(Context context, String label, String text) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);

        // Check to see if size is too large.
        try {
            clipboard.setPrimaryClip(ClipData.newPlainText(label, text));
            ToastUtil.showToast(context,"copy");
        }
        catch(RuntimeException e) {
            Throwable cause = e.getCause();
            if(cause instanceof android.os.TransactionTooLargeException) {
                ToastUtil.showToast(context,"clipboard_text_too_large");
            }
            else {
                // Something else went wrong. Just rethrow the original exception.
                throw(e);
            }
        }
    }

    public static CharSequence paste(Context context) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);

        if(!clipboard.hasPrimaryClip() || !clipboard.getPrimaryClipDescription().hasMimeType(MIMETYPE_TEXT_PLAIN)) {
            ToastUtil.showToast(context,"clipboard_not_text");
            return null;
        }
        else if(clipboard.getPrimaryClip().getItemAt(0).getText() == null || "".contentEquals(clipboard.getPrimaryClip().getItemAt(0).getText())) {
            ToastUtil.showToast(context,"clipboard_empty");
            return null;
        }
        else{
            ToastUtil.showToast(context,"paste");
            return clipboard.getPrimaryClip().getItemAt(0).getText();
        }
    }

    public static CharSequence getText(Context context) {
        // Get Clipboard text without showing any messages.
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);

        if(!clipboard.hasPrimaryClip() || !clipboard.getPrimaryClipDescription().hasMimeType(MIMETYPE_TEXT_PLAIN)) {
            return null;
        }
        else if(clipboard.getPrimaryClip().getItemAt(0).getText() == null || "".contentEquals(clipboard.getPrimaryClip().getItemAt(0).getText())) {
            return null;
        }
        else{
            return clipboard.getPrimaryClip().getItemAt(0).getText();
        }
    }
}
