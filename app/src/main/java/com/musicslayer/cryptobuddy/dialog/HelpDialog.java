package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.musicslayer.cryptobuddy.R;

public class HelpDialog extends BaseDialog {
    String helpText;

    public HelpDialog(Activity activity, String helpText) {
        super(activity);
        this.helpText = helpText;
    }

    public int getBaseViewID() {
        return R.id.help_dialog;
    }

    public void createLayout(Bundle savedInstanceState) {
        setContentView(R.layout.dialog_help);

        TextView T = findViewById(R.id.help_dialog_textView);
        T.setText(helpText);
    }
}