package com.musicslayer.cryptobuddy.view;

import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.asset.tokenmanager.TokenManager;
import com.musicslayer.cryptobuddy.dialog.BaseDialogFragment;
import com.musicslayer.cryptobuddy.dialog.ConfirmDeleteTokensDialog;
import com.musicslayer.cryptobuddy.dialog.ProgressDialog;
import com.musicslayer.cryptobuddy.dialog.ProgressDialogFragment;
import com.musicslayer.cryptobuddy.util.Toast;

public class TokenManagerView extends TableRow {
    public TextView T;
    public AppCompatButton B_DELETE;
    public AppCompatButton B_DOWNLOAD;
    public TokenManager tokenManager;
    public String tokenJSON = null;

    public TokenManagerView(Context context) {
        super(context);
    }

    public TokenManagerView(Context context, TokenManager tokenManager) {
        super(context);
        this.tokenManager = tokenManager;
        this.makeLayout(context);
    }

    public void makeLayout(Context context) {
        LinearLayout L = new LinearLayout(context);
        L.setGravity(Gravity.CENTER_VERTICAL);

        T = new TextView(context);

        ProgressDialogFragment progressDialogFragment = ProgressDialogFragment.newInstance(ProgressDialog.class);
        progressDialogFragment.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                queryTokens(context);
            }
        });
        progressDialogFragment.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                updateTokens(context);
            }
        });
        progressDialogFragment.restoreListeners(context, "progress");

        BaseDialogFragment confirmDeleteTokensDialogFragment = BaseDialogFragment.newInstance(ConfirmDeleteTokensDialog.class, tokenManager.getTokenType());
        confirmDeleteTokensDialogFragment.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if(((ConfirmDeleteTokensDialog)dialog).isComplete) {
                    tokenManager.resetDownloadedTokens();
                    tokenManager.resetFoundTokens();
                    tokenManager.resetCustomTokens();

                    tokenManager.save(context, "downloaded");
                    tokenManager.save(context, "found");
                    tokenManager.save(context, "custom");

                    updateLayout(context);
                    Toast.showToast("tokens_deleted");
                }
            }
        });
        confirmDeleteTokensDialogFragment.restoreListeners(context, "delete");

        B_DELETE = new AppCompatButton(context);
        B_DELETE.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_delete_24, 0, 0, 0);
        B_DELETE.setText("Delete");
        B_DELETE.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                confirmDeleteTokensDialogFragment.show(context, "delete");
            }
        });

        B_DOWNLOAD = new AppCompatButton(context);
        B_DOWNLOAD.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_download_24, 0, 0, 0);
        B_DOWNLOAD.setText("Download");
        B_DOWNLOAD.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                progressDialogFragment.show(context, "progress");
            }
        });

        L.addView(B_DELETE);
        L.addView(T);

        this.addView(L);
        this.addView(B_DOWNLOAD);
    }

    public void updateLayout(Context context) {
        // Still set the Button text so that spacing is easier to manage.
        //B_DOWNLOAD.setText("Download\n" + tokenManager.getTokenType() + " Tokens");

        if(tokenManager.canGetJSON()) {
            T.setText(tokenManager.getTokenType() + ":\n(" + tokenManager.downloaded_tokens.size() + ", " + tokenManager.found_tokens.size() + ", " + tokenManager.custom_tokens.size() + ")");
        }
        else {
            T.setText(tokenManager.getTokenType() + ":\n(" + tokenManager.found_tokens.size() + ", " + tokenManager.custom_tokens.size() + ")");
            B_DOWNLOAD.setVisibility(INVISIBLE);
        }
    }

    public void queryTokens(Context context) {
        tokenJSON = tokenManager.getJSON();
    }

    public void updateTokens(Context context) {
        if(tokenJSON != null) {
            tokenManager.resetDownloadedTokens();
            tokenManager.parse(tokenJSON);
            tokenManager.save(context, "downloaded");

            updateLayout(context);
        }
    }
}
