package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.data.persistent.app.PersistentAppDataStore;
import com.musicslayer.cryptobuddy.data.persistent.app.Policy;
import com.musicslayer.cryptobuddy.util.FileUtil;

public class DisclaimerDialog extends BaseDialog {
    public DisclaimerDialog(Activity activity) {
        super(activity);
    }

    @Override
    public void onBackPressedImpl() {
        // User cannot hit back to dismiss. They must select an option.
    }

    public int getBaseViewID() {
        return R.id.disclaimer_dialog;
    }

    public void createLayout(Bundle savedInstanceState) {
        setContentView(R.layout.dialog_disclaimer);

        String disclaimerText = FileUtil.readFile(R.raw.policy_disclaimer);
        TextView T = findViewById(R.id.disclaimer_textView);
        T.setText("To use this app, you must agree to the following disclaimer:\n\n" + disclaimerText);

        Button B_AGREEDISCLAIMER = findViewById(R.id.disclaimer_agreeButton);
        B_AGREEDISCLAIMER.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                PersistentAppDataStore.getInstance(Policy.class).setAgreeDisclaimer();
                dismiss();
            }
        });

        Button B_EXIT = findViewById(R.id.disclaimer_exitButton);
        B_EXIT.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            @Override
            public void onClickImpl(View v) {
                // For this dialog, "isComplete" means we have to exit the app.
                isComplete = true;
                dismiss();
            }
        });
    }
}