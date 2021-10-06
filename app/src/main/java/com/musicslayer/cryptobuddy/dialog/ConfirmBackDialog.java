package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.view.View;
import android.widget.Button;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.crash.CrashOnClickListener;

public class ConfirmBackDialog extends BaseDialog {
    public ConfirmBackDialog(Activity activity) {
        super(activity);
    }

    @Override
    public void onBackPressedImpl() {
        // User cannot hit back to dismiss. They must select an option.
    }

    public int getBaseViewID() {
        return R.id.confirm_back_dialog;
    }

    public void createLayout() {
        setContentView(R.layout.dialog_confirm_back);

        Button B_GOBACK = findViewById(R.id.confirm_back_dialog_backButton);
        B_GOBACK.setOnClickListener(new CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                isComplete = true;
                dismiss();
            }
        });

        Button B_CANCEL = findViewById(R.id.confirm_back_dialog_cancelButton);
        B_CANCEL.setOnClickListener(new CrashOnClickListener(this.activity) {
            @Override
            public void onClickImpl(View v) {
                dismiss();
            }
        });
    }
}
