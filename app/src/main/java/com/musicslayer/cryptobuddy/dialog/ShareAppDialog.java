package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.util.MessageUtil;
import com.musicslayer.cryptobuddy.R;

public class ShareAppDialog extends BaseDialog {
    public ShareAppDialog(Activity activity) {
        super(activity);
    }

    public int getBaseViewID() {
        return R.id.share_app_dialog;
    }

    public void createLayout(Bundle savedInstanceState) {
        setContentView(R.layout.dialog_share_app);

        Button B_EMAIL = findViewById(R.id.share_app_dialog_emailButton);
        B_EMAIL.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                MessageUtil.sendEmail(ShareAppDialog.this.activity, "", "Crypto Buddy - Your friend wants to recommend this app to you!", "Hey! You should check out this new app I found called Crypto Buddy!\n\nhttp://play.google.com/store/apps/details?id=com.musicslayer.cryptobuddy", null);
            }
        });

        Button B_SMS = findViewById(R.id.share_app_dialog_smsButton);
        B_SMS.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                MessageUtil.sendSMS(ShareAppDialog.this.activity, "Hey! You should check out this new app I found called Crypto Buddy!\n\nhttp://play.google.com/store/apps/details?id=com.musicslayer.cryptobuddy");
            }
        });
    }
}