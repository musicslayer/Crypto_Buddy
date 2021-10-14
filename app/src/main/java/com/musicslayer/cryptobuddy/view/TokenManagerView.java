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

import java.lang.ref.WeakReference;

public class TokenManagerView extends CrashTableRow {
    WeakReference<BaseDialogFragment> confirmDeleteTokensDialogFragment_w;
    WeakReference<BaseDialogFragment> deleteTokensDialogFragment_w;
    WeakReference<BaseDialogFragment> downloadTokensDialogFragment_w;
    WeakReference<ProgressDialogFragment> progressDirectDialogFragment_w;
    WeakReference<ProgressDialogFragment> progressFixedDialogFragment_w;

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

        progressFixedDialogFragment_w = new WeakReference<>(ProgressDialogFragment.newInstance(ProgressDialog.class));
        progressFixedDialogFragment_w.get().setOnShowListener(new CrashDialogInterface.CrashOnShowListener(context) {
            @Override
            public void onShowImpl(DialogInterface dialog) {
                String tokenJSON = tokenManager.getFixedJSON();
                ProgressDialogFragment.setValue(Serialization.string_serialize(tokenJSON));
            }
        });
        progressFixedDialogFragment_w.get().setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(context) {
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
        progressFixedDialogFragment_w.get().restoreListeners(context, "progress_fixed_" + tokenManager.getSettingsKey());

        progressDirectDialogFragment_w = new WeakReference<>(ProgressDialogFragment.newInstance(ProgressDialog.class));
        progressDirectDialogFragment_w.get().setOnShowListener(new CrashDialogInterface.CrashOnShowListener(context) {
            @Override
            public void onShowImpl(DialogInterface dialog) {
                String tokenJSON = tokenManager.getJSON();
                ProgressDialogFragment.setValue(Serialization.string_serialize(tokenJSON));
            }
        });
        progressDirectDialogFragment_w.get().setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(context) {
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
        progressDirectDialogFragment_w.get().restoreListeners(context, "progress_direct_" + tokenManager.getSettingsKey());

        confirmDeleteTokensDialogFragment_w = new WeakReference<>(BaseDialogFragment.newInstance(ConfirmDeleteTokensDialog.class, tokenManager.getTokenType()));
        confirmDeleteTokensDialogFragment_w.get().setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(context) {
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
        confirmDeleteTokensDialogFragment_w.get().restoreListeners(context, "confirm_delete_" + tokenManager.getSettingsKey());

        deleteTokensDialogFragment_w = new WeakReference<>(BaseDialogFragment.newInstance(DeleteTokensDialog.class, tokenManager.getTokenType(), tokenManager.canGetJSON()));
        deleteTokensDialogFragment_w.get().setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(context) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((DeleteTokensDialog)dialog).isComplete) {
                    choice = ((DeleteTokensDialog)dialog).user_CHOICE;
                    confirmDeleteTokensDialogFragment_w.get().show(context, "confirm_delete_" + tokenManager.getSettingsKey());
                }
            }
        });
        deleteTokensDialogFragment_w.get().restoreListeners(context, "delete_" + tokenManager.getSettingsKey());

        B_DELETE = new AppCompatButton(context);
        B_DELETE.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_delete_24, 0, 0, 0);
        B_DELETE.setText("Delete");
        B_DELETE.setOnClickListener(new CrashView.CrashOnClickListener(context) {
            public void onClickImpl(View v) {
                deleteTokensDialogFragment_w.get().show(context, "delete_" + tokenManager.getSettingsKey());
            }
        });

        downloadTokensDialogFragment_w = new WeakReference<>(BaseDialogFragment.newInstance(DownloadTokensDialog.class, tokenManager.getTokenType()));
        downloadTokensDialogFragment_w.get().setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(context) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((DownloadTokensDialog)dialog).isComplete) {
                    if(((DownloadTokensDialog)dialog).isFixed) {
                        progressFixedDialogFragment_w.get().show(context, "progress_fixed_" + tokenManager.getSettingsKey());
                    }
                    else {
                        progressDirectDialogFragment_w.get().show(context, "progress_direct_" + tokenManager.getSettingsKey());
                    }
                }
            }
        });
        downloadTokensDialogFragment_w.get().restoreListeners(context, "download_" + tokenManager.getSettingsKey());

        B_DOWNLOAD = new AppCompatButton(context);
        B_DOWNLOAD.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_download_24, 0, 0, 0);
        B_DOWNLOAD.setText("Download");
        B_DOWNLOAD.setOnClickListener(new CrashView.CrashOnClickListener(context) {
            public void onClickImpl(View v) {
                downloadTokensDialogFragment_w.get().show(context, "download_" + tokenManager.getSettingsKey());
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
