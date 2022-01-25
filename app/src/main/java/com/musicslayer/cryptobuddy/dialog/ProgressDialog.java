package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.musicslayer.cryptobuddy.R;

// This is only meant to be used with ProgressDialogFragment.
public class ProgressDialog extends BaseDialog {
    public boolean isCancelled = false;

    public ProgressDialog(Activity activity) {
        super(activity);
    }

    @Override
    public void onBackPressedImpl() {
        ProgressDialogFragment.setCancelled();
        dismiss();
    }

    public int getBaseViewID() {
        return R.id.progress_dialog;
    }

    public void createLayout(Bundle savedInstanceState) {
        setContentView(R.layout.dialog_progress);

        // TODO We want the background, but it needs to wrap the objects.
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        updateLayout();
    }

    public void updateLayout() {
        TextView T = findViewById(R.id.progress_dialog_progressTitle);
        T.setText(ProgressDialogFragment.progress_title[0]);

        TextView D = findViewById(R.id.progress_dialog_progressDisplay);
        D.setText(ProgressDialogFragment.progress_display[0]);
    }
}