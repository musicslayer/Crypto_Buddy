package com.musicslayer.cryptobuddy.view;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.asset.tokenmanager.TokenManager;
import com.musicslayer.cryptobuddy.crash.CrashDialogInterface;
import com.musicslayer.cryptobuddy.crash.CrashTableRow;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.dialog.BaseDialogFragment;
import com.musicslayer.cryptobuddy.dialog.ConfirmDeleteTokensDialog;
import com.musicslayer.cryptobuddy.dialog.DeleteTokensDialog;
import com.musicslayer.cryptobuddy.dialog.DownloadTokensDialog;
import com.musicslayer.cryptobuddy.dialog.ProgressDialog;
import com.musicslayer.cryptobuddy.dialog.ProgressDialogFragment;
import com.musicslayer.cryptobuddy.persistence.TokenManagerList;
import com.musicslayer.cryptobuddy.serialize.Serialization;
import com.musicslayer.cryptobuddy.util.ToastUtil;

public class TokenManagerView extends CrashTableRow {
    public TextView T;
    public AppCompatButton B_DELETE;
    public AppCompatButton B_DOWNLOAD;
    public TokenManager tokenManager;
    public String choice;

    public TokenManagerView(Context context) {
        super(context);
    }

    public TokenManagerView(Context context, TokenManager tokenManager) {
        super(context);
        this.tokenManager = tokenManager;
        this.makeLayout();
    }

    public void makeLayout() {
        Context context = getContext();

        LinearLayout L = new LinearLayout(context);
        L.setGravity(Gravity.CENTER_VERTICAL);

        T = new TextView(context);

        ProgressDialogFragment progressFixedDialogFragment = ProgressDialogFragment.newInstance(ProgressDialog.class);
        progressFixedDialogFragment.setOnShowListener(new CrashDialogInterface.CrashOnShowListener(context) {
            @Override
            public void onShowImpl(DialogInterface dialog) {
                String tokenJSON = tokenManager.getFixedJSON();
                ProgressDialogFragment.setValue(Serialization.string_serialize(tokenJSON));
            }
        });
        progressFixedDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(context) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                String tokenJSON = Serialization.string_deserialize(ProgressDialogFragment.getValue());

                boolean isComplete;
                if(tokenJSON == null) {
                    isComplete = false;
                }
                else {
                    tokenManager.resetDownloadedTokens();
                    isComplete = tokenManager.parseFixed(tokenJSON);

                    TokenManagerList.updateTokenManager(getContext(), tokenManager);

                    updateLayout();
                }

                if(!isComplete) {
                    ToastUtil.showToast(context,"tokens_not_downloaded");
                }
            }
        });
        progressFixedDialogFragment.restoreListeners(context, "progress_fixed_" + tokenManager.getSettingsKey());

        ProgressDialogFragment progressDirectDialogFragment = ProgressDialogFragment.newInstance(ProgressDialog.class);
        progressDirectDialogFragment.setOnShowListener(new CrashDialogInterface.CrashOnShowListener(context) {
            @Override
            public void onShowImpl(DialogInterface dialog) {
                String tokenJSON = tokenManager.getJSON();
                ProgressDialogFragment.setValue(Serialization.string_serialize(tokenJSON));
            }
        });
        progressDirectDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(context) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                String tokenJSON = Serialization.string_deserialize(ProgressDialogFragment.getValue());

                boolean isComplete;
                if(tokenJSON == null) {
                    isComplete = false;
                }
                else {
                    tokenManager.resetDownloadedTokens();
                    isComplete = tokenManager.parse(tokenJSON);

                    TokenManagerList.updateTokenManager(getContext(), tokenManager);

                    updateLayout();
                }

                if(!isComplete) {
                    ToastUtil.showToast(context,"tokens_not_downloaded");
                }
            }
        });
        progressDirectDialogFragment.restoreListeners(context, "progress_direct_" + tokenManager.getSettingsKey());

        BaseDialogFragment confirmDeleteTokensDialogFragment = BaseDialogFragment.newInstance(ConfirmDeleteTokensDialog.class, tokenManager.getTokenType());
        confirmDeleteTokensDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(context) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((ConfirmDeleteTokensDialog)dialog).isComplete) {
                    if("downloaded".equals(choice)) {
                        tokenManager.resetDownloadedTokens();
                    }
                    else if("found".equals(choice)) {
                        tokenManager.resetFoundTokens();
                    }
                    else if("custom".equals(choice)) {
                        tokenManager.resetCustomTokens();
                    }

                    TokenManagerList.updateTokenManager(getContext(), tokenManager);

                    updateLayout();
                    ToastUtil.showToast(context,"tokens_deleted");
                }
            }
        });
        confirmDeleteTokensDialogFragment.restoreListeners(context, "confirm_delete_" + tokenManager.getSettingsKey());

        BaseDialogFragment deleteTokensDialogFragment = BaseDialogFragment.newInstance(DeleteTokensDialog.class, tokenManager.getTokenType(), tokenManager.canGetJSON());
        deleteTokensDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(context) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((DeleteTokensDialog)dialog).isComplete) {
                    choice = ((DeleteTokensDialog)dialog).user_CHOICE;
                    confirmDeleteTokensDialogFragment.show(context, "confirm_delete_" + tokenManager.getSettingsKey());
                }
            }
        });
        deleteTokensDialogFragment.restoreListeners(context, "delete_" + tokenManager.getSettingsKey());

        B_DELETE = new AppCompatButton(context);
        B_DELETE.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_delete_24, 0, 0, 0);
        B_DELETE.setText("Delete");
        B_DELETE.setOnClickListener(new CrashView.CrashOnClickListener(context) {
            public void onClickImpl(View v) {
                deleteTokensDialogFragment.show(context, "delete_" + tokenManager.getSettingsKey());
            }
        });

        BaseDialogFragment downloadTokensDialogFragment = BaseDialogFragment.newInstance(DownloadTokensDialog.class, tokenManager.getTokenType());
        downloadTokensDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(context) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
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
        B_DOWNLOAD.setOnClickListener(new CrashView.CrashOnClickListener(context) {
            public void onClickImpl(View v) {
                downloadTokensDialogFragment.show(context, "download_" + tokenManager.getSettingsKey());
            }
        });

        L.addView(B_DELETE);
        L.addView(T);

        this.addView(L);
        this.addView(B_DOWNLOAD);
    }

    public void updateLayout() {
        if(tokenManager.canGetJSON()) {
            T.setText(tokenManager.getTokenType() + ":\n(" + tokenManager.downloaded_tokens.size() + ", " + tokenManager.found_tokens.size() + ", " + tokenManager.custom_tokens.size() + ")");
        }
        else {
            T.setText(tokenManager.getTokenType() + ":\n(" + tokenManager.found_tokens.size() + ", " + tokenManager.custom_tokens.size() + ")");
            B_DOWNLOAD.setVisibility(INVISIBLE);
        }
    }

    @Override
    public Parcelable onSaveInstanceStateImpl(Parcelable state)
    {
        Bundle bundle = new Bundle();
        bundle.putParcelable("superState", state);
        bundle.putString("choice", choice);

        return bundle;
    }

    @Override
    public Parcelable onRestoreInstanceStateImpl(Parcelable state)
    {
        if (state instanceof Bundle) // implicit null check
        {
            Bundle bundle = (Bundle) state;
            state = bundle.getParcelable("superState");
            choice = bundle.getString("choice");
        }
        return state;
    }
}
