package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.os.Bundle;

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
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
    }
}