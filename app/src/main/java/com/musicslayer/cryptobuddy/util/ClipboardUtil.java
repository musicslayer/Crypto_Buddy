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
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        boolean notText = !(clipboard.hasPrimaryClip() && clipboard.getPrimaryClipDescription().hasMimeType(MIMETYPE_TEXT_PLAIN));
        boolean isEmpty = "".contentEquals(clipboard.getPrimaryClip().getItemAt(0).getText());

        if(notText) {
            ToastUtil.showToast(context,"clipboard_not_text");
            return null;
        }
        else if(isEmpty) {
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
        boolean notText = !clipboard.hasPrimaryClip() && clipboard.getPrimaryClipDescription().hasMimeType(MIMETYPE_TEXT_PLAIN);
        boolean isEmpty = "".contentEquals(clipboard.getPrimaryClip().getItemAt(0).getText());

        if(notText) {
            return null;
        }
        else if(isEmpty) {
            return null;
        }
        else{
            return clipboard.getPrimaryClip().getItemAt(0).getText();
        }
    }
}
