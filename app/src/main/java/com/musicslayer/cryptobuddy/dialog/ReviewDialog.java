package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.Button;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.crash.CrashOnClickListener;
import com.musicslayer.cryptobuddy.util.Toast;

public class ReviewDialog extends BaseDialog {
    public boolean user_LATER = false;

    public ReviewDialog(Activity activity) {
        super(activity);
    }

    @Override
    public void onBackPressed() {
        // User cannot hit back to dismiss. They must select an option.
    }

    public int getBaseViewID() {
        return R.id.review_dialog;
    }

    public void createLayout() {
        setContentView(R.layout.dialog_review);

        Button B_YES = findViewById(R.id.review_dialog_dialog_yesButton);
        B_YES.setOnClickListener(new CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                // This can use Google Play app or the web browser.
                Intent reviewIntent = new Intent();
                reviewIntent.setAction(Intent.ACTION_VIEW);
                reviewIntent.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + activity.getPackageName()));

                ComponentName reviewApp = reviewIntent.resolveActivity(activity.getPackageManager());
                ComponentName unsupportedAction = ComponentName.unflattenFromString("com.android.fallback/.Fallback");
                if(reviewApp != null && !reviewApp.equals(unsupportedAction)) {
                    activity.startActivity(reviewIntent);
                }
                else {
                    Toast.showToast(activity,"review");
                }

                // Either button sets "isComplete" to true. We just want to make sure the user selected something.
                isComplete = true;
                dismiss();
            }
        });

        Button B_NO = findViewById(R.id.review_dialog_dialog_noButton);
        B_NO.setOnClickListener(new CrashOnClickListener(this.activity) {
            @Override
            public void onClickImpl(View v) {
                // Either button sets "isComplete" to true. We just want to make sure the user selected something.
                isComplete = true;
                dismiss();
            }
        });

        Button B_LATER = findViewById(R.id.review_dialog_dialog_laterButton);
        B_LATER.setOnClickListener(new CrashOnClickListener(this.activity) {
            @Override
            public void onClickImpl(View v) {
                // Either button sets "isComplete" to true. We just want to make sure the user selected something.
                user_LATER = true;

                isComplete = true;
                dismiss();
            }
        });
    }
}
