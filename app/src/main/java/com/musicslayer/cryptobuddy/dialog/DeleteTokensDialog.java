package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.crash.CrashOnClickListener;

public class DeleteTokensDialog extends BaseDialog {
    public String tokenString;
    public boolean canGetJSON;

    public String user_CHOICE;

    public DeleteTokensDialog(Activity activity, String tokenString, Boolean canGetJSON) {
        super(activity);
        this.tokenString = tokenString;
        this.canGetJSON = canGetJSON;
    }

    public int getBaseViewID() {
        return R.id.delete_tokens_dialog;
    }

    public void createLayout() {
        setContentView(R.layout.dialog_delete_tokens);

        Toolbar toolbar = findViewById(R.id.delete_tokens_dialog_toolbar);
        toolbar.setTitle("Delete " + tokenString + " Tokens");

        Button B_DOWNLOADED = findViewById(R.id.delete_tokens_dialog_downloadedButton);
        B_DOWNLOADED.setOnClickListener(new CrashOnClickListener(this.activity) {
            @Override
            public void onClickImpl(View v) {
                user_CHOICE = "downloaded";
                isComplete = true;
                dismiss();
            }
        });

        // Some token types can't be downloaded, so don't offer this deletion option.
        if(!canGetJSON) {
            TextView T_DOWNLOADED = findViewById(R.id.delete_tokens_dialog_downloadedTextView);
            T_DOWNLOADED.setVisibility(View.GONE);
            B_DOWNLOADED.setVisibility(View.GONE);

            TextView T_FOUND = findViewById(R.id.delete_tokens_dialog_foundTextView);
            ConstraintLayout.LayoutParams L = (ConstraintLayout.LayoutParams)T_FOUND.getLayoutParams();
            L.setMargins(0, 0, 0, 0);
            T_FOUND.setLayoutParams(L);
        }

        Button B_FOUND = findViewById(R.id.delete_tokens_dialog_foundButton);
        B_FOUND.setOnClickListener(new CrashOnClickListener(this.activity) {
            @Override
            public void onClickImpl(View v) {
                user_CHOICE = "found";
                isComplete = true;
                dismiss();
            }
        });

        Button B_CUSTOM = findViewById(R.id.delete_tokens_dialog_customButton);
        B_CUSTOM.setOnClickListener(new CrashOnClickListener(this.activity) {
            @Override
            public void onClickImpl(View v) {
                user_CHOICE = "custom";
                isComplete = true;
                dismiss();
            }
        });
    }
}
