package com.musicslayer.cryptobuddy.view.asset;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageButton;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.asset.tokenmanager.TokenManager;
import com.musicslayer.cryptobuddy.crash.CrashDialogInterface;
import com.musicslayer.cryptobuddy.crash.CrashTableRow;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.data.persistent.app.PersistentAppDataStore;
import com.musicslayer.cryptobuddy.dialog.BaseDialogFragment;
import com.musicslayer.cryptobuddy.dialog.ConfirmDeleteTokensDialog;
import com.musicslayer.cryptobuddy.dialog.DeleteTokensDialog;
import com.musicslayer.cryptobuddy.dialog.DownloadTokensDialog;
import com.musicslayer.cryptobuddy.dialog.ProgressDialog;
import com.musicslayer.cryptobuddy.dialog.ProgressDialogFragment;
import com.musicslayer.cryptobuddy.dialog.ViewTokensDialog;
import com.musicslayer.cryptobuddy.data.persistent.app.TokenManagerList;
import com.musicslayer.cryptobuddy.data.bridge.Serialization;
import com.musicslayer.cryptobuddy.util.ToastUtil;

import java.util.ArrayList;

public class TokenManagerView extends CrashTableRow {
    public TextView T;
    public AppCompatImageButton B_DELETE;
    public AppCompatImageButton B_VIEW;
    public AppCompatImageButton B_DOWNLOAD;
    public TokenManager tokenManager;
    public ArrayList<String> choices;

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
                ProgressDialogFragment.setValue(Serialization.serialize(tokenJSON, String.class));
            }
        });
        progressFixedDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(context) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                String tokenJSON = Serialization.deserialize(ProgressDialogFragment.getValue(), String.class);

                boolean isComplete;
                if(tokenJSON == null) {
                    isComplete = false;
                }
                else {
                    tokenManager.resetDownloadedTokens();
                    isComplete = tokenManager.parseFixed(tokenJSON);

                    PersistentAppDataStore.getInstance(TokenManagerList.class).updateTokenManager(tokenManager);

                    updateLayout();
                }

                if(!isComplete) {
                    ToastUtil.showToast("tokens_not_downloaded");
                }
            }
        });
        progressFixedDialogFragment.restoreListeners(context, "progress_fixed_tokens_" + tokenManager.getSettingsKey());

        ProgressDialogFragment progressDirectDialogFragment = ProgressDialogFragment.newInstance(ProgressDialog.class);
        progressDirectDialogFragment.setOnShowListener(new CrashDialogInterface.CrashOnShowListener(context) {
            @Override
            public void onShowImpl(DialogInterface dialog) {
                String tokenJSON = tokenManager.getJSON();
                ProgressDialogFragment.setValue(Serialization.serialize(tokenJSON, String.class));
            }
        });
        progressDirectDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(context) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                String tokenJSON = Serialization.deserialize(ProgressDialogFragment.getValue(), String.class);

                boolean isComplete;
                if(tokenJSON == null) {
                    isComplete = false;
                }
                else {
                    tokenManager.resetDownloadedTokens();
                    isComplete = tokenManager.parse(tokenJSON);

                    PersistentAppDataStore.getInstance(TokenManagerList.class).updateTokenManager(tokenManager);

                    updateLayout();
                }

                if(!isComplete) {
                    ToastUtil.showToast("tokens_not_downloaded");
                }
            }
        });
        progressDirectDialogFragment.restoreListeners(context, "progress_direct_tokens_" + tokenManager.getSettingsKey());

        BaseDialogFragment confirmDeleteTokensDialogFragment = BaseDialogFragment.newInstance(ConfirmDeleteTokensDialog.class, tokenManager.getTokenType());
        confirmDeleteTokensDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(context) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((ConfirmDeleteTokensDialog)dialog).isComplete) {
                    if(choices.contains("downloaded")) {
                        tokenManager.resetDownloadedTokens();
                    }
                    if(choices.contains("found")) {
                        tokenManager.resetFoundTokens();
                    }
                    if(choices.contains("custom")) {
                        tokenManager.resetCustomTokens();
                    }

                    PersistentAppDataStore.getInstance(TokenManagerList.class).updateTokenManager(tokenManager);

                    updateLayout();
                }
            }
        });
        confirmDeleteTokensDialogFragment.restoreListeners(context, "confirm_delete_tokens_" + tokenManager.getSettingsKey());

        BaseDialogFragment deleteTokensDialogFragment = BaseDialogFragment.newInstance(DeleteTokensDialog.class, tokenManager.getTokenType(), tokenManager.canGetJSON());
        deleteTokensDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(context) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((DeleteTokensDialog)dialog).isComplete) {
                    choices = ((DeleteTokensDialog)dialog).user_CHOICES;
                    confirmDeleteTokensDialogFragment.show(context, "confirm_delete_tokens_" + tokenManager.getSettingsKey());
                }
            }
        });
        deleteTokensDialogFragment.restoreListeners(context, "delete_tokens_" + tokenManager.getSettingsKey());

        B_DELETE = new AppCompatImageButton(context);
        B_DELETE.setImageResource(R.drawable.ic_baseline_delete_24);
        B_DELETE.setOnClickListener(new CrashView.CrashOnClickListener(context) {
            public void onClickImpl(View v) {
                deleteTokensDialogFragment.show(context, "delete_tokens_" + tokenManager.getSettingsKey());
            }
        });

        B_VIEW = new AppCompatImageButton(context);
        B_VIEW.setImageResource(R.drawable.ic_baseline_pageview_24);
        B_VIEW.setOnClickListener(new CrashView.CrashOnClickListener(context) {
            public void onClickImpl(View v) {
                BaseDialogFragment.newInstance(ViewTokensDialog.class, tokenManager.getTokenType(), tokenManager.canGetJSON()).show(context, "view_" + tokenManager.getSettingsKey());
            }
        });

        BaseDialogFragment downloadTokensDialogFragment = BaseDialogFragment.newInstance(DownloadTokensDialog.class, tokenManager.getTokenType());
        downloadTokensDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(context) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((DownloadTokensDialog)dialog).isComplete) {
                    if(((DownloadTokensDialog)dialog).isFixed) {
                        progressFixedDialogFragment.show(context, "progress_fixed_tokens_" + tokenManager.getSettingsKey());
                    }
                    else {
                        progressDirectDialogFragment.show(context, "progress_direct_tokens_" + tokenManager.getSettingsKey());
                    }
                }
            }
        });
        downloadTokensDialogFragment.restoreListeners(context, "download_tokens_" + tokenManager.getSettingsKey());

        B_DOWNLOAD = new AppCompatImageButton(context);
        B_DOWNLOAD.setImageResource(R.drawable.ic_baseline_download_24);
        B_DOWNLOAD.setOnClickListener(new CrashView.CrashOnClickListener(context) {
            public void onClickImpl(View v) {
                downloadTokensDialogFragment.show(context, "download_tokens_" + tokenManager.getSettingsKey());
            }
        });

        L.addView(B_DELETE);
        L.addView(B_VIEW);
        L.addView(B_DOWNLOAD);

        this.addView(L);
        this.addView(T);
    }

    public void updateLayout() {
        if(tokenManager.canGetJSON()) {
            T.setText(tokenManager.getTokenType() + ":\n(" + tokenManager.downloaded_tokens.size() + ", " + tokenManager.found_tokens.size() + ", " + tokenManager.custom_tokens.size() + ")");
        }
        else {
            T.setText(tokenManager.getTokenType() + ":\n(-, " + tokenManager.found_tokens.size() + ", " + tokenManager.custom_tokens.size() + ")");
            B_DOWNLOAD.setVisibility(INVISIBLE);
        }
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
