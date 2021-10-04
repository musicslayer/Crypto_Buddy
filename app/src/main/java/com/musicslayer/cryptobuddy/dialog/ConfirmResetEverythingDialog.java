package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.view.View;
import android.widget.Button;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.crash.CrashOnClickListener;
import com.musicslayer.cryptobuddy.view.ConfirmationView;

public class ConfirmResetEverythingDialog extends BaseDialog {
    public ConfirmResetEverythingDialog(Activity activity) {
        super(activity);
    }

    public int getBaseViewID() {
        return R.id.confirm_reset_everything_dialog;
    }

    public void createLayout() {
        setContentView(R.layout.dialog_confirm_reset_everything);

        ConfirmationView C = findViewById(R.id.confirm_reset_everything_dialog_confirmationView);
        C.setNumDigits(activity, 8);
        C.setOnConfirmationListener(new ConfirmationView.ConfirmationListener() {
            @Override
            public void onConfirmation(ConfirmationView confirmationView) {
                isComplete = true;
                dismiss();
            }
        });

        Button B_CANCEL = findViewById(R.id.confirm_reset_everything_dialog_cancelButton);
        B_CANCEL.setOnClickListener(new CrashOnClickListener(this.activity) {
            @Override
            public void onClickImpl(View v) {
                dismiss();
            }
        });
    }
}
