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
            ToastUtil.showToast(context,"copy_clipboard_success");
        }
        catch(RuntimeException e) {
            if(e.getCause() instanceof android.os.TransactionTooLargeException) {
                ToastUtil.showToast(context,"copy_clipboard_text_too_large");
            }
            else {
                // Something else went wrong but we don't know what.
                ToastUtil.showToast(context,"copy_clipboard_unknown_error");
            }
        }
    }

    public static void exportText(Context context, String label, String text) {
        // Export to the clipboard, which shows different messages than "copy".
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);

        // Check to see if size is too large.
        try {
            clipboard.setPrimaryClip(ClipData.newPlainText(label, text));
            ToastUtil.showToast(context,"export_clipboard_success");
        }
        catch(RuntimeException e) {
            if(e.getCause() instanceof android.os.TransactionTooLargeException) {
                ToastUtil.showToast(context,"export_clipboard_text_too_large");
            }
            else {
                // Something else went wrong but we don't know what.
                ToastUtil.showToast(context,"export_clipboard_unknown_error");
            }
        }
    }

    public static CharSequence paste(Context context) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);

        if(!clipboard.hasPrimaryClip() || !clipboard.getPrimaryClipDescription().hasMimeType(MIMETYPE_TEXT_PLAIN)) {
            ToastUtil.showToast(context,"paste_clipboard_not_text");
            return null;
        }
        else if(clipboard.getPrimaryClip().getItemAt(0).getText() == null || "".contentEquals(clipboard.getPrimaryClip().getItemAt(0).getText())) {
            ToastUtil.showToast(context,"paste_clipboard_empty");
            return null;
        }
        else{
            ToastUtil.showToast(context,"paste_clipboard_success");
            return clipboard.getPrimaryClip().getItemAt(0).getText();
        }
    }

    public static CharSequence importText(Context context) {
        // Import from the clipboard, which shows different messages than "paste".
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);

        if(!clipboard.hasPrimaryClip() || !clipboard.getPrimaryClipDescription().hasMimeType(MIMETYPE_TEXT_PLAIN)) {
            ToastUtil.showToast(context,"import_clipboard_not_text");
            return null;
        }
        else if(clipboard.getPrimaryClip().getItemAt(0).getText() == null || "".contentEquals(clipboard.getPrimaryClip().getItemAt(0).getText())) {
            ToastUtil.showToast(context,"import_clipboard_empty");
            return null;
        }
        else{
            // Don't show any message here, because we still don't know if the text is a valid format to be imported.
            return clipboard.getPrimaryClip().getItemAt(0).getText();
        }
    }
}
