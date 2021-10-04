package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.widget.Toolbar;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.crash.CrashOnClickListener;

public class DownloadTokensDialog extends BaseDialog {
    public String tokenString;
    public boolean isFixed;

    public DownloadTokensDialog(Activity activity, String tokenString) {
        super(activity);
        this.tokenString = tokenString;
    }

    public int getBaseViewID() {
        return R.id.download_tokens_dialog;
    }

    public void createLayout() {
        setContentView(R.layout.dialog_download_tokens);

        Toolbar toolbar = findViewById(R.id.download_tokens_dialog_toolbar);
        toolbar.setTitle("Download " + tokenString + " Tokens");

        Button B_FIXED = findViewById(R.id.download_tokens_dialog_fixedButton);
        B_FIXED.setOnClickListener(new CrashOnClickListener(this.activity) {
            @Override
            public void onClickImpl(View v) {
                isFixed = true;
                isComplete = true;
                dismiss();
            }
        });

        Button B_DIRECT = findViewById(R.id.download_tokens_dialog_directButton);
        B_DIRECT.setOnClickListener(new CrashOnClickListener(this.activity) {
            @Override
            public void onClickImpl(View v) {
                isFixed = false;
                isComplete = true;
                dismiss();
            }
        });
    }
}
