package com.musicslayer.cryptobuddy.util;

import android.content.Context;

import com.musicslayer.cryptobuddy.dialog.BaseDialogFragment;
import com.musicslayer.cryptobuddy.dialog.HelpDialog;

public class Help {
    public static void showHelp(Context context, int id) {
        String helpText = File.readFile(context, id);

        BaseDialogFragment helpDialogFragment = BaseDialogFragment.newInstance(HelpDialog.class, helpText);
        helpDialogFragment.show(context, "help");
    }
}
