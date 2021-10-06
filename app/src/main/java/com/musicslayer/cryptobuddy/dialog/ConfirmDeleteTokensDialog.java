package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.widget.Toolbar;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.view.ConfirmationView;

public class ConfirmDeleteTokensDialog extends BaseDialog {
    String tokenString;

    public ConfirmDeleteTokensDialog(Activity activity, String tokenString) {
        super(activity);
        this.tokenString = tokenString;
    }

    public int getBaseViewID() {
        return R.id.confirm_delete_tokens_dialog;
    }

    public void createLayout() {
        setContentView(R.layout.dialog_confirm_delete_tokens);

        Toolbar toolbar = findViewById(R.id.confirm_delete_tokens_dialog_toolbar);
        toolbar.setTitle("Delete " + tokenString + " Tokens?");

        ConfirmationView C = findViewById(R.id.confirm_delete_tokens_dialog_confirmationView);
        C.setOnConfirmationListener(new ConfirmationView.ConfirmationListener() {
            @Override
            public void onConfirmation(ConfirmationView confirmationView) {
                isComplete = true;
                dismiss();
            }
        });

        Button B_CANCEL = findViewById(R.id.confirm_delete_tokens_dialog_cancelButton);
        B_CANCEL.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            @Override
            public void onClickImpl(View v) {
                dismiss();
            }
        });
    }
}
