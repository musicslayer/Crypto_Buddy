package com.musicslayer.cryptobuddy.view.settings;

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.crash.CrashDialogInterface;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.dialog.BaseDialogFragment;
import com.musicslayer.cryptobuddy.dialog.ConfirmResetEverythingDialog;
import com.musicslayer.cryptobuddy.persistence.Persistence;
import com.musicslayer.cryptobuddy.settings.setting.Setting;
import com.musicslayer.cryptobuddy.util.ToastUtil;

public class ResetEverythingSettingsView extends SettingsView {
    public ResetEverythingSettingsView(Context context) {
        super(context);
    }

    public ResetEverythingSettingsView(Context context, Setting setting) {
        super(context);

        this.setOrientation(VERTICAL);

        LayoutParams LP = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        LP.setMargins(0,0,0,50);
        this.setLayoutParams(LP);

        final TextView T_Reset=new TextView(context);
        T_Reset.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        T_Reset.setText("RESET EVERYTHING!\nApp will be like a new install. Purchase data will be temporarily reset, but will restore itself automatically shortly after restarting the app.");

        BaseDialogFragment confirmResetEverythingDialogFragment = BaseDialogFragment.newInstance(ConfirmResetEverythingDialog.class);
        confirmResetEverythingDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(context) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((ConfirmResetEverythingDialog)dialog).isComplete) {
                    boolean isComplete = Persistence.resetAllData(context);
                    if(isComplete) {
                        ToastUtil.showToast(context,"reset_everything");
                    }
                    else {
                        ToastUtil.showToast(context,"reset_everything_fail");
                    }
                }
            }
        });
        confirmResetEverythingDialogFragment.restoreListeners(context, "reset_reset_everything_settings_view");

        final AppCompatButton B_Reset = new AppCompatButton(context);
        B_Reset.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        B_Reset.setText("RESET EVERYTHING!");
        B_Reset.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_warning_24, 0, 0, 0);
        B_Reset.setOnClickListener(new CrashView.CrashOnClickListener(context) {
            public void onClickImpl(View v) {
                confirmResetEverythingDialogFragment.show(context, "reset_reset_everything_settings_view");
            }
        });

        this.addView(T_Reset);
        this.addView(B_Reset);
    }
}
