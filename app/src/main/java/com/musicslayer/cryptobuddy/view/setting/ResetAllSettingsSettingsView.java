package com.musicslayer.cryptobuddy.view.setting;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.dialog.ConfirmResetSettingsDialog;
import com.musicslayer.cryptobuddy.dialog.BaseDialogFragment;
import com.musicslayer.cryptobuddy.persistence.Settings;
import com.musicslayer.cryptobuddy.util.Toast;

public class ResetAllSettingsSettingsView extends LinearLayout {
    public ResetAllSettingsSettingsView(Context context) {
        super(context);

        this.setOrientation(VERTICAL);

        LinearLayout.LayoutParams LP = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        LP.setMargins(0,0,0,50);
        this.setLayoutParams(LP);

        final TextView T_Reset=new TextView(context);
        T_Reset.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        T_Reset.setText("Reset all settings to default values.");

        BaseDialogFragment confirmResetSettingsDialogFragment = BaseDialogFragment.newInstance(ConfirmResetSettingsDialog.class);
        confirmResetSettingsDialogFragment.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if(((ConfirmResetSettingsDialog)dialog).isComplete) {
                    Settings.resetAllSettings(context);
                    ((Activity)context).recreate();
                    Toast.showToast("reset_settings");
                }
            }
        });
        confirmResetSettingsDialogFragment.restoreListeners(context, "reset");

        final AppCompatButton B_Reset = new AppCompatButton(context);
        B_Reset.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        B_Reset.setText("Reset All Settings");
        B_Reset.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_warning_24, 0, 0, 0);
        B_Reset.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                confirmResetSettingsDialogFragment.show(context, "reset");
            }
        });

        this.addView(T_Reset);
        this.addView(B_Reset);
    }
}
