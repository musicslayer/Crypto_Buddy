package com.musicslayer.cryptobuddy.view.settings;

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.asset.tokenmanager.TokenManager;
import com.musicslayer.cryptobuddy.crash.CrashDialogInterface;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.dialog.BaseDialogFragment;
import com.musicslayer.cryptobuddy.dialog.ConfirmDeleteTokensDialog;
import com.musicslayer.cryptobuddy.persistence.TokenManagerList;
import com.musicslayer.cryptobuddy.settings.setting.Setting;
import com.musicslayer.cryptobuddy.util.ToastUtil;

public class DeleteDownloadedTokensSettingsView extends SettingsView {
    public DeleteDownloadedTokensSettingsView(Context context) {
        super(context);
    }

    public DeleteDownloadedTokensSettingsView(Context context, Setting setting) {
        super(context);

        this.setOrientation(VERTICAL);

        LayoutParams LP = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        LP.setMargins(0,0,0,50);
        this.setLayoutParams(LP);

        final TextView T_Reset=new TextView(context);
        T_Reset.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        T_Reset.setText("Delete all downloaded tokens from the app's database.");

        BaseDialogFragment confirmDeleteTokensDialogFragment = BaseDialogFragment.newInstance(ConfirmDeleteTokensDialog.class, "Downloaded");
        confirmDeleteTokensDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(context) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((ConfirmDeleteTokensDialog)dialog).isComplete) {
                    TokenManager.resetAllDownloadedTokens();
                    TokenManagerList.saveAllData(context);
                    ToastUtil.showToast(context,"reset_downloaded_tokens");
                }
            }
        });
        confirmDeleteTokensDialogFragment.restoreListeners(context, "reset_delete_downloaded_tokens_settings_view");

        final AppCompatButton B_DELETE = new AppCompatButton(context);
        B_DELETE.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        B_DELETE.setText("Delete All Downloaded Tokens");
        B_DELETE.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_warning_24, 0, 0, 0);
        B_DELETE.setOnClickListener(new CrashView.CrashOnClickListener(context) {
            public void onClickImpl(View v) {
                confirmDeleteTokensDialogFragment.show(context, "reset_delete_downloaded_tokens_settings_view");
            }
        });

        this.addView(T_Reset);
        this.addView(B_DELETE);
    }
}
