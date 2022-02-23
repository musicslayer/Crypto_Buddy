package com.musicslayer.cryptobuddy.view.settings;

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.crash.CrashDialogInterface;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.dialog.ConfirmResetSettingsDialog;
import com.musicslayer.cryptobuddy.dialog.BaseDialogFragment;
import com.musicslayer.cryptobuddy.data.persistent.user.PersistentUserDataStore;
import com.musicslayer.cryptobuddy.data.persistent.user.SettingList;
import com.musicslayer.cryptobuddy.settings.setting.Setting;
import com.musicslayer.cryptobuddy.util.ContextUtil;
import com.musicslayer.cryptobuddy.util.ToastUtil;

public class ResetAllSettingsSettingsView extends SettingsView {
    public ResetAllSettingsSettingsView(Context context) {
        super(context);
    }

    public ResetAllSettingsSettingsView(Context context, Setting setting) {
        super(context);

        this.setOrientation(VERTICAL);

        LinearLayout.LayoutParams LP = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        LP.setMargins(0,0,0,50);
        this.setLayoutParams(LP);

        final TextView T_Reset=new TextView(context);
        T_Reset.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        T_Reset.setText("Reset all settings to default values.");

        BaseDialogFragment confirmResetSettingsDialogFragment = BaseDialogFragment.newInstance(ConfirmResetSettingsDialog.class);
        confirmResetSettingsDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(context) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((ConfirmResetSettingsDialog)dialog).isComplete) {
                    // TODO Non-standard reset?
                    Setting.resetAllSettings();

                    // Clear out any existing data, and then make sure our recently defaulted settings are saved.
                    PersistentUserDataStore.getInstance(SettingList.class).resetAllData();
                    PersistentUserDataStore.getInstance(SettingList.class).saveAllData();

                    ToastUtil.showToast("reset_settings");
                }
            }
        });
        confirmResetSettingsDialogFragment.restoreListeners(context, "reset_reset_all_settings_view");

        final AppCompatButton B_Reset = new AppCompatButton(context);
        B_Reset.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        B_Reset.setText("Reset All Settings");
        B_Reset.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_warning_24, 0, 0, 0);
        B_Reset.setOnClickListener(new CrashView.CrashOnClickListener(context) {
            public void onClickImpl(View v) {
                confirmResetSettingsDialogFragment.show(context, "reset_reset_all_settings_view");
            }
        });

        this.addView(T_Reset);
        this.addView(B_Reset);
    }
}
