package com.musicslayer.cryptobuddy.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TableLayout;

import androidx.annotation.NonNull;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.asset.tokenmanager.TokenManager;
import com.musicslayer.cryptobuddy.crash.CrashDialogInterface;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.dialog.AddCustomTokenDialog;
import com.musicslayer.cryptobuddy.dialog.BaseDialogFragment;
import com.musicslayer.cryptobuddy.dialog.DownloadTokensDialog;
import com.musicslayer.cryptobuddy.dialog.ProgressDialog;
import com.musicslayer.cryptobuddy.dialog.ProgressDialogFragment;
import com.musicslayer.cryptobuddy.persistence.TokenManagerList;
import com.musicslayer.cryptobuddy.serialize.Serialization;
import com.musicslayer.cryptobuddy.util.ThrowableUtil;
import com.musicslayer.cryptobuddy.util.HelpUtil;
import com.musicslayer.cryptobuddy.util.RESTUtil;
import com.musicslayer.cryptobuddy.util.ToastUtil;
import com.musicslayer.cryptobuddy.view.TokenManagerView;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class TokenManagerActivity extends BaseActivity {
    WeakReference<BaseDialogFragment> addCustomTokenDialogFragment_w;
    WeakReference<BaseDialogFragment> downloadTokensDialogFragment_w;
    WeakReference<ProgressDialogFragment> progressFixedDialogFragment_w;
    WeakReference<ProgressDialogFragment> progressDirectDialogFragment_w;

    ArrayList<TokenManagerView> tokenManagerViewArrayList;

    public int getAdLayoutViewID() {
        return R.id.token_manager_adLayout;
    }

    @Override
    public void onBackPressedImpl() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    public void createLayout () {
        setContentView(R.layout.activity_token_manager);

        ImageButton helpButton = findViewById(R.id.token_manager_helpButton);
        helpButton.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                HelpUtil.showHelp(TokenManagerActivity.this, R.raw.help_token_manager);
            }
        });

        TableLayout tableLayout = findViewById(R.id.token_manager_tableLayout);

        ArrayList<String> tokenTypes = TokenManager.tokenManagers_token_types;
        Collections.sort(tokenTypes, Comparator.comparing(String::toLowerCase));

        tokenManagerViewArrayList = new ArrayList<>();
        for(String tokenType : tokenTypes) {
            TokenManager tokenManager = TokenManager.getTokenManagerFromTokenType(tokenType);
            TokenManagerView tokenManagerView = new TokenManagerView(TokenManagerActivity.this, tokenManager);
            tokenManagerView.updateLayout();

            tokenManagerViewArrayList.add(tokenManagerView);
            tableLayout.addView(tokenManagerView);
        }

        addCustomTokenDialogFragment_w = new WeakReference<>(BaseDialogFragment.newInstance(AddCustomTokenDialog.class));
        addCustomTokenDialogFragment_w.get().setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((AddCustomTokenDialog)dialog).isComplete) {
                    // We don't know which view was changed, so just update all of them.
                    for(TokenManagerView tokenManagerView : tokenManagerViewArrayList) {
                        tokenManagerView.updateLayout();
                    }
                }
            }
        });
        addCustomTokenDialogFragment_w.get().restoreListeners(this, "add_custom_token");

        Button B_CustomToken = findViewById(R.id.token_manager_customTokenButton);
        B_CustomToken.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                addCustomTokenDialogFragment_w.get().show(TokenManagerActivity.this, "add_custom_token");
            }
        });

        progressFixedDialogFragment_w = new WeakReference<>(ProgressDialogFragment.newInstance(ProgressDialog.class));
        progressFixedDialogFragment_w.get().setOnShowListener(new CrashDialogInterface.CrashOnShowListener(this) {
            @Override
            public void onShowImpl(DialogInterface dialog) {
                String tokenAllJSON = RESTUtil.get("https://raw.githubusercontent.com/musicslayer/token_hub/main/token_info/ALL");
                ProgressDialogFragment.setValue(Serialization.string_serialize(tokenAllJSON));
            }
        });
        progressFixedDialogFragment_w.get().setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                String tokenAllJSON = Serialization.string_deserialize(ProgressDialogFragment.getValue());

                if(tokenAllJSON == null) {
                    ToastUtil.showToast(TokenManagerActivity.this,"tokens_not_downloaded");
                }
                else {
                    JSONObject tokenAllJSONObject;
                    try {
                        tokenAllJSONObject = new JSONObject(tokenAllJSON);
                    }
                    catch(Exception e) {
                        ThrowableUtil.processThrowable(e);
                        ToastUtil.showToast(TokenManagerActivity.this,"tokens_not_downloaded");
                        return;
                    }

                    boolean isAllComplete = true;

                    // Update everything even if one of them wasn't complete.
                    for(TokenManagerView tokenManagerView : tokenManagerViewArrayList) {
                        String settingsKey = tokenManagerView.tokenManager.getSettingsKey();

                        // Not all token types can have a fixed list of tokens.
                        if(!tokenAllJSONObject.has(settingsKey)) {
                            continue;
                        }

                        JSONObject tokenTypeJSON;
                        try {
                            tokenTypeJSON = tokenAllJSONObject.getJSONObject(settingsKey);
                        }
                        catch(Exception e) {
                            ThrowableUtil.processThrowable(e);
                            isAllComplete = false;
                            continue;
                        }

                        String tokenJSON = tokenTypeJSON.toString();

                        tokenManagerView.tokenManager.resetDownloadedTokens();
                        tokenManagerView.tokenManager.parseFixed(tokenJSON);
                        TokenManagerList.updateTokenManager(TokenManagerActivity.this, tokenManagerView.tokenManager);

                        tokenManagerView.updateLayout();
                    }

                    if(!isAllComplete) {
                        ToastUtil.showToast(TokenManagerActivity.this,"tokens_not_downloaded");
                    }
                }
            }
        });
        progressFixedDialogFragment_w.get().restoreListeners(this, "progress_fixed");

        progressDirectDialogFragment_w = new WeakReference<>(ProgressDialogFragment.newInstance(ProgressDialog.class));
        progressDirectDialogFragment_w.get().setOnShowListener(new CrashDialogInterface.CrashOnShowListener(this) {
            @Override
            public void onShowImpl(DialogInterface dialog) {
                ArrayList<String> tokenJSONArrayList = new ArrayList<>();

                for(TokenManagerView tokenManagerView : tokenManagerViewArrayList) {
                    tokenJSONArrayList.add(tokenManagerView.tokenManager.getJSON());
                }

                ProgressDialogFragment.setValue(Serialization.string_serializeArrayList(tokenJSONArrayList));
            }
        });
        progressDirectDialogFragment_w.get().setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                ArrayList<String> tokenJSONArrayList = Serialization.string_deserializeArrayList(ProgressDialogFragment.getValue());

                boolean isAllComplete = true;

                for(int i = 0; i < tokenManagerViewArrayList.size(); i++) {
                    TokenManagerView tokenManagerView = tokenManagerViewArrayList.get(i);

                    tokenManagerView.tokenManager.resetDownloadedTokens();
                    boolean isComplete = tokenManagerView.tokenManager.parse(tokenJSONArrayList.get(i));
                    TokenManagerList.updateTokenManager(TokenManagerActivity.this, tokenManagerView.tokenManager);

                    tokenManagerView.updateLayout();
                    if(!isComplete) {
                        isAllComplete = false;
                    }
                }

                if(!isAllComplete) {
                    ToastUtil.showToast(TokenManagerActivity.this,"tokens_not_downloaded");
                }
            }
        });
        progressDirectDialogFragment_w.get().restoreListeners(this, "progress_direct");

        downloadTokensDialogFragment_w = new WeakReference<>(BaseDialogFragment.newInstance(DownloadTokensDialog.class, "All"));
        downloadTokensDialogFragment_w.get().setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((DownloadTokensDialog)dialog).isComplete) {
                    if(((DownloadTokensDialog)dialog).isFixed) {
                        progressFixedDialogFragment_w.get().show(TokenManagerActivity.this, "progress_fixed");
                    }
                    else {
                        progressDirectDialogFragment_w.get().show(TokenManagerActivity.this, "progress_direct");
                    }
                }
            }
        });
        downloadTokensDialogFragment_w.get().restoreListeners(this, "download");

        Button B_MassUpdate = findViewById(R.id.token_manager_massUpdateButton);
        B_MassUpdate.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                downloadTokensDialogFragment_w.get().show(TokenManagerActivity.this, "download");
            }
        });
    }

    @Override
    public void onSaveInstanceStateImpl(@NonNull Bundle bundle) {
        for(TokenManagerView tokenManagerView : tokenManagerViewArrayList) {
            bundle.putParcelable("tokenManagerView_" + tokenManagerView.tokenManager.getTokenType(), tokenManagerView.onSaveInstanceState());
        }
    }

    @Override
    public void onRestoreInstanceStateImpl(Bundle bundle) {
        if(bundle != null) {
            for(TokenManagerView tokenManagerView : tokenManagerViewArrayList) {
                tokenManagerView.onRestoreInstanceState(bundle.getParcelable("tokenManagerView_" + tokenManagerView.tokenManager.getTokenType()));
            }
        }
    }
}