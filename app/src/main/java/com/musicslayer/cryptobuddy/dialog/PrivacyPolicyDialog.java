package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.text.util.Linkify;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.persistence.PrivacyPolicy;

public class PrivacyPolicyDialog extends BaseDialog {
    public PrivacyPolicyDialog(Activity activity) {
        super(activity);
    }

    @Override
    public void onBackPressed() {
        // User cannot hit back to dismiss. They must select an option.
    }

    public int getBaseViewID() {
        return R.id.privacy_policy_dialog;
    }

    public void createLayout() {
        setContentView(R.layout.dialog_privacy_policy);

        TextView T = findViewById(R.id.privacy_policy_textView);
        T.setAutoLinkMask(Linkify.WEB_URLS);
        T.setText("To use this app, you must agree to the privacy policy:\n" + "https://sites.google.com/view/crypto-buddy-privacy-policy/home\n\n");

        Button B_AGREE = findViewById(R.id.privacy_policy_agreeButton);
        B_AGREE.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                PrivacyPolicy.setAgree(PrivacyPolicyDialog.this.activity);
                dismiss();
            }
        });

        Button B_EXIT = findViewById(R.id.privacy_policy_exitButton);
        B_EXIT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // For this dialog, "isComplete" means we have to exit the app.
                isComplete = true;
                dismiss();
            }
        });
    }
}