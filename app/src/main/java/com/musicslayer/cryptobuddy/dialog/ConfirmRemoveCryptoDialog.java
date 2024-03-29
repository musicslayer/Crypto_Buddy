package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.settings.setting.ConfirmationSetting;
import com.musicslayer.cryptobuddy.view.ConfirmationView;

public class ConfirmRemoveCryptoDialog extends BaseDialog {
    public ConfirmRemoveCryptoDialog(Activity activity) {
        super(activity);
    }

    public int getBaseViewID() {
        return R.id.confirm_remove_crypto_dialog;
    }

    @Override
    public void showImpl() {
        super.showImpl();

        // Early return if confirmations are off.
        if("None".equals(ConfirmationSetting.value)) {
            isComplete = true;
            dismiss();
        }
    }

    public void createLayout(Bundle savedInstanceState) {
        setContentView(R.layout.dialog_confirm_remove_crypto);

        ConfirmationView C = findViewById(R.id.confirm_remove_crypto_dialog_confirmationView);
        C.setOnConfirmationListener(new ConfirmationView.ConfirmationListener() {
            @Override
            public void onConfirmation(ConfirmationView confirmationView) {
                isComplete = true;
                dismiss();
            }
        });

        Button B_CANCEL = findViewById(R.id.confirm_remove_crypto_dialog_cancelButton);
        B_CANCEL.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            @Override
            public void onClickImpl(View v) {
                dismiss();
            }
        });
    }
}
