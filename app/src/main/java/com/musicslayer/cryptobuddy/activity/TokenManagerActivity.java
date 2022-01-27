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
import com.musicslayer.cryptobuddy.util.WebUtil;
import com.musicslayer.cryptobuddy.util.ToastUtil;
import com.musicslayer.cryptobuddy.view.TokenManagerView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

// TODO TokenManager, let people view tokens.

public class TokenManagerActivity extends BaseActivity {
    ArrayList<TokenManagerView> tokenManagerViewArrayList;

    @Override
    public int getAdLayoutViewID() {
        return R.id.token_manager_adLayout;
    }

    @Override
    public void onBackPressedImpl() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    public void createLayout(Bundle savedInstanceState) {
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

        BaseDialogFragment addCustomTokenDialogFragment = BaseDialogFragment.newInstance(AddCustomTokenDialog.class);
        addCustomTokenDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((AddCustomTokenDialog)dialog).isComplete) {
                    // It's easier to just update all of them.
                    for(TokenManagerView tokenManagerView : tokenManagerViewArrayList) {
                        tokenManagerView.updateLayout();
                    }
                }
            }
        });
        addCustomTokenDialogFragment.restoreListeners(this, "add_custom_token");

        Button B_CustomToken = findViewById(R.id.token_manager_customTokenButton);
        B_CustomToken.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                addCustomTokenDialogFragment.show(TokenManagerActivity.this, "add_custom_token");
            }
        });

        ProgressDialogFragment progressFixedDialogFragment = ProgressDialogFragment.newInstance(ProgressDialog.class);
        progressFixedDialogFragment.setOnShowListener(new CrashDialogInterface.CrashOnShowListener(this) {
            @Override
            public void onShowImpl(DialogInterface dialog) {
                ProgressDialogFragment.updateProgressTitle("Downloading All Tokens...");

                String tokenAllJSON = WebUtil.get("https://raw.githubusercontent.com/musicslayer/token_hub/main/token_info/ALL");
                ProgressDialogFragment.setValue(Serialization.string_serialize(tokenAllJSON));
            }
        });
        progressFixedDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
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
        progressFixedDialogFragment.restoreListeners(this, "progress_fixed");

        ProgressDialogFragment progressDirectDialogFragment = ProgressDialogFragment.newInstance(ProgressDialog.class);
        progressDirectDialogFragment.setOnShowListener(new CrashDialogInterface.CrashOnShowListener(this) {
            @Override
            public void onShowImpl(DialogInterface dialog) {
                ProgressDialogFragment.updateProgressTitle("Downloading All Tokens...");

                ArrayList<String> tokenJSONArrayList = new ArrayList<>();

                for(TokenManagerView tokenManagerView : tokenManagerViewArrayList) {
                    if(ProgressDialogFragment.isCancelled()) { return; }
                    tokenJSONArrayList.add(tokenManagerView.tokenManager.getJSON());
                }

                ProgressDialogFragment.setValue(Serialization.string_serializeArrayList(tokenJSONArrayList));
            }
        });
        progressDirectDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
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
        progressDirectDialogFragment.restoreListeners(this, "progress_direct");

        BaseDialogFragment downloadTokensDialogFragment = BaseDialogFragment.newInstance(DownloadTokensDialog.class, "All");
        downloadTokensDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((DownloadTokensDialog)dialog).isComplete) {
                    if(((DownloadTokensDialog)dialog).isFixed) {
                        progressFixedDialogFragment.show(TokenManagerActivity.this, "progress_fixed");
                    }
                    else {
                        progressDirectDialogFragment.show(TokenManagerActivity.this, "progress_direct");
                    }
                }
            }
        });
        downloadTokensDialogFragment.restoreListeners(this, "download");

        Button B_MassUpdate = findViewById(R.id.token_manager_massUpdateButton);
        B_MassUpdate.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                downloadTokensDialogFragment.show(TokenManagerActivity.this, "download");
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