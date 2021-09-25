package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.widget.TextView;

import com.musicslayer.cryptobuddy.R;

public class InfoDialog extends BaseDialog {
    String infoText;

    public InfoDialog(Activity activity, String infoText) {
        super(activity);
        this.infoText = infoText;
    }

    public int getBaseViewID() {
        return R.id.info_dialog;
    }

    public void createLayout () {
        setContentView(R.layout.dialog_info);

        TextView T = findViewById(R.id.info_dialog_textView);
        T.setText(infoText);
    }
}