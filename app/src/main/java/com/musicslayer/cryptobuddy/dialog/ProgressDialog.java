package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
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

    public void adjustDialog() {
        // Do nothing here. ProgressDialog will be exactly large enough to wrap all its content.
    }

    public void createLayout(Bundle savedInstanceState) {
        setContentView(R.layout.dialog_progress);
        updateLayout();
    }

    public void updateLayout() {
        TextView T = findViewById(R.id.progress_dialog_progressTitle);
        T.setText(ProgressDialogFragment.progress_title[0]);
        T.setVisibility(ProgressDialogFragment.progress_title[0].isEmpty() ? View.GONE : View.VISIBLE);

        TextView D = findViewById(R.id.progress_dialog_progressDisplay);
        D.setText(ProgressDialogFragment.progress_display[0]);
        D.setVisibility(ProgressDialogFragment.progress_display[0].isEmpty() ? View.GONE : View.VISIBLE);
    }
}