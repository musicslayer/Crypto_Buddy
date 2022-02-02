package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.crash.CrashView;

public class DeleteFiatsDialog extends BaseDialog {
    public String user_CHOICE;

    public DeleteFiatsDialog(Activity activity) {
        super(activity);
    }

    public int getBaseViewID() {
        return R.id.delete_fiats_dialog;
    }

    public void createLayout(Bundle savedInstanceState) {
        setContentView(R.layout.dialog_delete_fiats);

        Button B_FOUND = findViewById(R.id.delete_fiats_dialog_foundButton);
        B_FOUND.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            @Override
            public void onClickImpl(View v) {
                user_CHOICE = "found";
                isComplete = true;
                dismiss();
            }
        });

        Button B_CUSTOM = findViewById(R.id.delete_fiats_dialog_customButton);
        B_CUSTOM.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            @Override
            public void onClickImpl(View v) {
                user_CHOICE = "custom";
                isComplete = true;
                dismiss();
            }
        });
    }
}
