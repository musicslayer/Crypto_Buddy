package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.widget.Toolbar;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.settings.setting.ConfirmationSetting;
import com.musicslayer.cryptobuddy.view.ConfirmationView;

public class ConfirmDeleteTokensDialog extends BaseDialog {
    String tokenType;

    public ConfirmDeleteTokensDialog(Activity activity, String tokenType) {
        super(activity);
        this.tokenType = tokenType;
    }

    public int getBaseViewID() {
        return R.id.confirm_delete_tokens_dialog;
    }

    @Override
    public void showImpl() {
        super.showImpl();

        // Early return if confirmations are off.
        if(!"All".equals(tokenType)) {
            if("None".equals(ConfirmationSetting.value)) {
                isComplete = true;
                dismiss();
            }
        }
    }

    public void createLayout(Bundle savedInstanceState) {
        setContentView(R.layout.dialog_confirm_delete_tokens);

        Toolbar toolbar = findViewById(R.id.confirm_delete_tokens_dialog_toolbar);
        toolbar.setTitle("Delete " + tokenType + " Tokens?");

        ConfirmationView C = findViewById(R.id.confirm_delete_tokens_dialog_confirmationView);
        if("All".equals(tokenType)) {
            C.setNumDigits(8);
            C.setStrict(true);
        }
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
