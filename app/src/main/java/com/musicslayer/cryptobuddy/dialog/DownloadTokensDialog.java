package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.widget.Toolbar;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.crash.CrashView;

public class DownloadTokensDialog extends BaseDialog {
    public String tokenType;
    public boolean isFixed;

    public DownloadTokensDialog(Activity activity, String tokenType) {
        super(activity);
        this.tokenType = tokenType;
    }

    public int getBaseViewID() {
        return R.id.download_tokens_dialog;
    }

    public void createLayout(Bundle savedInstanceState) {
        setContentView(R.layout.dialog_download_tokens);

        Toolbar toolbar = findViewById(R.id.download_tokens_dialog_toolbar);
        toolbar.setTitle("Download " + tokenType + " Tokens");

        Button B_FIXED = findViewById(R.id.download_tokens_dialog_fixedButton);
        B_FIXED.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            @Override
            public void onClickImpl(View v) {
                isFixed = true;
                isComplete = true;
                dismiss();
            }
        });

        Button B_DIRECT = findViewById(R.id.download_tokens_dialog_directButton);
        B_DIRECT.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            @Override
            public void onClickImpl(View v) {
                isFixed = false;
                isComplete = true;
                dismiss();
            }
        });
    }
}
