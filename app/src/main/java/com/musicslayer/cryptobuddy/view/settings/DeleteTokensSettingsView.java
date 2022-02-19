package com.musicslayer.cryptobuddy.view.settings;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.asset.tokenmanager.TokenManager;
import com.musicslayer.cryptobuddy.crash.CrashDialogInterface;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.dialog.BaseDialogFragment;
import com.musicslayer.cryptobuddy.dialog.ConfirmDeleteTokensDialog;
import com.musicslayer.cryptobuddy.dialog.DeleteTokensDialog;
import com.musicslayer.cryptobuddy.persistence.PersistentDataStore;
import com.musicslayer.cryptobuddy.persistence.TokenManagerList;
import com.musicslayer.cryptobuddy.settings.setting.Setting;
import com.musicslayer.cryptobuddy.util.ToastUtil;

import java.util.ArrayList;

public class DeleteTokensSettingsView extends SettingsView {
    public ArrayList<String> choices;

    public DeleteTokensSettingsView(Context context) {
        super(context);
    }

    public DeleteTokensSettingsView(Context context, Setting setting) {
        super(context);

        this.setOrientation(VERTICAL);

        LayoutParams LP = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        LP.setMargins(0,0,0,50);
        this.setLayoutParams(LP);

        final TextView T_Reset=new TextView(context);
        T_Reset.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        T_Reset.setText("Delete all tokens from the app's database.");

        BaseDialogFragment confirmDeleteTokensDialogFragment = BaseDialogFragment.newInstance(ConfirmDeleteTokensDialog.class, "All");
        confirmDeleteTokensDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(context) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((ConfirmDeleteTokensDialog)dialog).isComplete) {
                    if(choices.contains("downloaded")) {
                        TokenManager.resetAllDownloadedTokens();
                    }
                    if(choices.contains("found")) {
                        TokenManager.resetAllFoundTokens();
                    }
                    if(choices.contains("custom")) {
                        TokenManager.resetAllCustomTokens();
                    }

                    PersistentDataStore.getInstance(TokenManagerList.class).saveAllData();
                    ToastUtil.showToast("reset_tokens");
                }
            }
        });
        confirmDeleteTokensDialogFragment.restoreListeners(context, "reset_confirm_delete_tokens_settings_view");

        BaseDialogFragment deleteTokensDialogFragment = BaseDialogFragment.newInstance(DeleteTokensDialog.class, "All", true);
        deleteTokensDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(context) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((DeleteTokensDialog)dialog).isComplete) {
                    choices = ((DeleteTokensDialog)dialog).user_CHOICES;
                    confirmDeleteTokensDialogFragment.show(context, "reset_confirm_delete_tokens_settings_view");
                }
            }
        });
        deleteTokensDialogFragment.restoreListeners(context, "reset_delete_tokens_settings_view");

        final AppCompatButton B_DELETE = new AppCompatButton(context);
        B_DELETE.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        B_DELETE.setText("Delete All Tokens");
        B_DELETE.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_warning_24, 0, 0, 0);
        B_DELETE.setOnClickListener(new CrashView.CrashOnClickListener(context) {
            public void onClickImpl(View v) {
                deleteTokensDialogFragment.show(context, "reset_delete_tokens_settings_view");
            }
        });

        this.addView(T_Reset);
        this.addView(B_DELETE);
    }

    @Override
    public Parcelable onSaveInstanceStateImpl(Parcelable state)
    {
        Bundle bundle = new Bundle();
        bundle.putParcelable("superState", state);
        bundle.putStringArrayList("choices", choices);

        return bundle;
    }

    @Override
    public Parcelable onRestoreInstanceStateImpl(Parcelable state)
    {
        if (state instanceof Bundle) // implicit null check
        {
            Bundle bundle = (Bundle) state;
            state = bundle.getParcelable("superState");
            choices = bundle.getStringArrayList("choices");
        }
        return state;
    }
}
