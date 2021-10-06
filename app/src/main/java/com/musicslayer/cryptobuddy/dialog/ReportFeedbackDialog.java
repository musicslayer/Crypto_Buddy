package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.view.View;
import android.widget.Button;

import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.util.MessageUtil;
import com.musicslayer.cryptobuddy.R;

public class ReportFeedbackDialog extends BaseDialog {
    public ReportFeedbackDialog(Activity activity) {
        super(activity);
    }

    public int getBaseViewID() {
        return R.id.report_feedback_dialog;
    }

    public void createLayout() {
        setContentView(R.layout.dialog_report_feedback);

        Button B_EMAIL = findViewById(R.id.report_feedback_dialog_button);
        B_EMAIL.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                MessageUtil.sendEmail(ReportFeedbackDialog.this.activity, "musicslayer@gmail.com", "Crypto Buddy - Bug Report/Feedback", "", null);
            }
        });
    }
}