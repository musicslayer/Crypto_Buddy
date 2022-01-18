package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.os.Bundle;
import android.text.util.Linkify;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.persistence.Policy;

public class PrivacyPolicyDialog extends BaseDialog {
    public PrivacyPolicyDialog(Activity activity) {
        super(activity);
    }

    @Override
    public void onBackPressedImpl() {
        // User cannot hit back to dismiss. They must select an option.
    }

    public int getBaseViewID() {
        return R.id.privacy_policy_dialog;
    }

    public void createLayout(Bundle savedInstanceState) {
        setContentView(R.layout.dialog_privacy_policy);

        TextView T = findViewById(R.id.privacy_policy_textView);
        T.setAutoLinkMask(Linkify.WEB_URLS);
        T.setText("To use this app, you must agree to the privacy policy:\n" + "https://sites.google.com/view/crypto-buddy-privacy-policy/home\n\n");

        Button B_AGREEPRIVACYPOLICY = findViewById(R.id.privacy_policy_agreeButton);
        B_AGREEPRIVACYPOLICY.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                Policy.setAgreePrivacyPolicy(PrivacyPolicyDialog.this.activity);
                dismiss();
            }
        });

        Button B_EXIT = findViewById(R.id.privacy_policy_exitButton);
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