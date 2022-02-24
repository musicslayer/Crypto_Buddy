package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.os.Bundle;

import com.musicslayer.cryptobuddy.R;

// This dialog only exists to block user input.
public class TransparentDialog extends BaseDialog {
    public TransparentDialog(Activity activity) {
        super(activity);
    }

    @Override
    public void onBackPressedImpl() {
        // User cannot hit back to dismiss.
    }

    public int getBaseViewID() {
        return R.id.transparent_dialog;
    }

    public void adjustDialog() {
        // Do nothing here.
    }

    public void createLayout(Bundle savedInstanceState) {
        setContentView(R.layout.dialog_transparent);
        getWindow().setDimAmount(0); // Remove grey background.
    }
}