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
        TextView T_TITLE = findViewById(R.id.progress_dialog_progressTitle);
        if(ProgressDialogFragment.progress_title[0] == null) {
            T_TITLE.setVisibility(View.GONE);
        }
        else {
            T_TITLE.setText(ProgressDialogFragment.progress_title[0]);
            T_TITLE.setVisibility(View.VISIBLE);
        }

        TextView T_SUBTITLE = findViewById(R.id.progress_dialog_progressSubtitle);
        if(ProgressDialogFragment.progress_subtitle[0] == null) {
            T_SUBTITLE.setVisibility(View.GONE);
        }
        else {
            T_SUBTITLE.setText(ProgressDialogFragment.progress_subtitle[0]);
            T_SUBTITLE.setVisibility(View.VISIBLE);
        }

        TextView T_DISPLAY = findViewById(R.id.progress_dialog_progressDisplay);
        if(ProgressDialogFragment.progress_display[0] == null) {
            T_DISPLAY.setVisibility(View.GONE);
        }
        else {
            T_DISPLAY.setText(ProgressDialogFragment.progress_display[0]);
            T_DISPLAY.setVisibility(View.VISIBLE);
        }
    }
}