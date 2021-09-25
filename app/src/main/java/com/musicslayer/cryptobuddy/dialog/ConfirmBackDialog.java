package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.view.View;
import android.widget.Button;

import com.musicslayer.cryptobuddy.R;

public class ConfirmBackDialog extends BaseDialog {
    public ConfirmBackDialog(Activity activity) {
        super(activity);
    }

    @Override
    public void onBackPressed() {
        // User cannot hit back to dismiss. They must select an option.
    }

    public int getBaseViewID() {
        return R.id.confirm_back_dialog;
    }

    public void createLayout() {
        setContentView(R.layout.dialog_confirm_back);

        Button B_GOBACK = findViewById(R.id.confirm_back_dialog_backButton);
        B_GOBACK.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                isComplete = true;
                dismiss();
            }
        });

        Button B_CANCEL = findViewById(R.id.confirm_back_dialog_cancelButton);
        B_CANCEL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }
}
