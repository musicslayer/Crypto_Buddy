package com.musicslayer.cryptobuddy.view.setting;

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.asset.tokenmanager.TokenManager;
import com.musicslayer.cryptobuddy.dialog.BaseDialogFragment;
import com.musicslayer.cryptobuddy.dialog.ConfirmDeleteTokensDialog;
import com.musicslayer.cryptobuddy.persistence.TokenList;
import com.musicslayer.cryptobuddy.util.Toast;

public class DeleteDownloadedTokensSettingsView extends LinearLayout {
    public DeleteDownloadedTokensSettingsView(Context context) {
        super(context);

        this.setOrientation(VERTICAL);

        LayoutParams LP = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        LP.setMargins(0,0,0,50);
        this.setLayoutParams(LP);

        final TextView T_Reset=new TextView(context);
        T_Reset.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        T_Reset.setText("Delete all downloaded tokens from the app's database.");

        BaseDialogFragment confirmDeleteTokensDialogFragment = BaseDialogFragment.newInstance(ConfirmDeleteTokensDialog.class, "Downloaded", "");
        confirmDeleteTokensDialogFragment.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if(((ConfirmDeleteTokensDialog)dialog).isComplete) {
                    TokenList.resetDownloadedTokens(context);
                    TokenManager.resetAllDownloadedTokens();
                    Toast.showToast("reset_downloaded_tokens");
                }
            }
        });
        confirmDeleteTokensDialogFragment.restoreListeners(context, "delete");

        final AppCompatButton B_DELETE = new AppCompatButton(context);
        B_DELETE.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        B_DELETE.setText("Delete All Downloaded Tokens");
        B_DELETE.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_warning_24, 0, 0, 0);
        B_DELETE.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                confirmDeleteTokensDialogFragment.show(context, "delete");
            }
        });

        this.addView(T_Reset);
        this.addView(B_DELETE);
    }
}
