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
import com.musicslayer.cryptobuddy.dialog.DeleteTokensDialog;
import com.musicslayer.cryptobuddy.dialog.DownloadTokensDialog;
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

        ProgressDialogFragment progressFixedDialogFragment = ProgressDialogFragment.newInstance(ProgressDialog.class);
        progressFixedDialogFragment.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                queryTokensFixed(context);
            }
        });
        progressFixedDialogFragment.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                updateTokensFixed(context);
            }
        });
        progressFixedDialogFragment.restoreListeners(context, "progress_fixed_" + tokenManager.getSettingsKey());

        ProgressDialogFragment progressDirectDialogFragment = ProgressDialogFragment.newInstance(ProgressDialog.class);
        progressDirectDialogFragment.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                queryTokensDirect(context);
            }
        });
        progressDirectDialogFragment.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                updateTokensDirect(context);
            }
        });
        progressDirectDialogFragment.restoreListeners(context, "progress_direct_" + tokenManager.getSettingsKey());

        BaseDialogFragment confirmDeleteTokensDialogFragment = BaseDialogFragment.newInstance(ConfirmDeleteTokensDialog.class, tokenManager.getTokenType(), "");
        confirmDeleteTokensDialogFragment.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if(((ConfirmDeleteTokensDialog)dialog).isComplete) {
                    String choice = ((ConfirmDeleteTokensDialog)dialog).choice;
                    if("downloaded".equals(choice)) {
                        tokenManager.resetDownloadedTokens();
                        tokenManager.save(context, "downloaded");
                    }
                    else if("found".equals(choice)) {
                        tokenManager.resetFoundTokens();
                        tokenManager.save(context, "found");
                    }
                    else if("custom".equals(choice)) {
                        tokenManager.resetCustomTokens();
                        tokenManager.save(context, "custom");
                    }

                    updateLayout(context);
                    Toast.showToast("tokens_deleted");
                }
            }
        });
        confirmDeleteTokensDialogFragment.restoreListeners(context, "confirm_delete_" + tokenManager.getSettingsKey());

        BaseDialogFragment deleteTokensDialogFragment = BaseDialogFragment.newInstance(DeleteTokensDialog.class, tokenManager.getTokenType(), tokenManager.canGetJSON());
        deleteTokensDialogFragment.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if(((DeleteTokensDialog)dialog).isComplete) {
                    confirmDeleteTokensDialogFragment.updateArguments(ConfirmDeleteTokensDialog.class, tokenManager.getTokenType(), ((DeleteTokensDialog)dialog).user_CHOICE);
                    confirmDeleteTokensDialogFragment.show(context, "confirm_delete_" + tokenManager.getSettingsKey());
                }
            }
        });
        deleteTokensDialogFragment.restoreListeners(context, "delete_" + tokenManager.getSettingsKey());

        B_DELETE = new AppCompatButton(context);
        B_DELETE.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_delete_24, 0, 0, 0);
        B_DELETE.setText("Delete");
        B_DELETE.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                deleteTokensDialogFragment.show(context, "delete_" + tokenManager.getSettingsKey());
            }
        });

        BaseDialogFragment downloadTokensDialogFragment = BaseDialogFragment.newInstance(DownloadTokensDialog.class, tokenManager.getTokenType());
        downloadTokensDialogFragment.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if(((DownloadTokensDialog)dialog).isComplete) {
                    if(((DownloadTokensDialog)dialog).isFixed) {
                        progressFixedDialogFragment.show(context, "progress_fixed_" + tokenManager.getSettingsKey());
                    }
                    else {
                        progressDirectDialogFragment.show(context, "progress_direct_" + tokenManager.getSettingsKey());
                    }
                }
            }
        });
        downloadTokensDialogFragment.restoreListeners(context, "download_" + tokenManager.getSettingsKey());

        B_DOWNLOAD = new AppCompatButton(context);
        B_DOWNLOAD.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_download_24, 0, 0, 0);
        B_DOWNLOAD.setText("Download");
        B_DOWNLOAD.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                downloadTokensDialogFragment.show(context, "download_" + tokenManager.getSettingsKey());
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

    public void queryTokensFixed(Context context) {
        tokenJSON = tokenManager.getFixedJSON();
    }

    public void updateTokensFixed(Context context) {
        if(tokenJSON != null) {
            tokenManager.resetDownloadedTokens();
            tokenManager.parseFixed(tokenJSON);
            tokenManager.save(context, "downloaded");

            updateLayout(context);
        }
    }

    public void queryTokensDirect(Context context) {
        tokenJSON = tokenManager.getJSON();
    }

    public void updateTokensDirect(Context context) {
        if(tokenJSON != null) {
            tokenManager.resetDownloadedTokens();
            tokenManager.parse(tokenJSON);
            tokenManager.save(context, "downloaded");

            updateLayout(context);
        }
    }
}
