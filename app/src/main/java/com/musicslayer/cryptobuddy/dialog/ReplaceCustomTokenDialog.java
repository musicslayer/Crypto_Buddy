package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.asset.crypto.token.Token;
import com.musicslayer.cryptobuddy.crash.CrashView;

public class ReplaceCustomTokenDialog extends BaseDialog {
    public Token oldToken;
    public Token newToken;

    public ReplaceCustomTokenDialog(Activity activity, Token oldToken, Token newToken) {
        super(activity);
        this.oldToken = oldToken;
        this.newToken = newToken;
    }

    public int getBaseViewID() {
        return R.id.replace_custom_token_dialog;
    }

    public void createLayout() {
        setContentView(R.layout.dialog_replace_custom_token);

        TextView T = findViewById(R.id.replace_custom_token_dialog_textView);
        String text = "A custom token with this ID already exists. Would you like to replace it?\n\n" +
            "Existing Token:\n" +
            "  ID = " + oldToken.getID() + "\n" +
            "  Name = " + oldToken.getDisplayName() + "\n" +
            "  Symbol = " + oldToken.getName() + "\n" +
            "  Decimals = " + oldToken.getScale() + "\n" +
            "\nNew Token:\n" +
            "  ID = " + newToken.getID() + "\n" +
            "  Name = " + newToken.getDisplayName() + "\n" +
            "  Symbol = " + newToken.getName() + "\n" +
            "  Decimals = " + newToken.getScale();

        T.setText(text);

        Button cancelButton = findViewById(R.id.replace_custom_token_dialog_cancelButton);
        cancelButton.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            @Override
            public void onClickImpl(View view) {
                dismiss();
            }
        });

        Button confirmButton = findViewById(R.id.replace_custom_token_dialog_confirmButton);
        confirmButton.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            @Override
            public void onClickImpl(View view) {
                isComplete = true;
                dismiss();
            }
        });
    }
}
