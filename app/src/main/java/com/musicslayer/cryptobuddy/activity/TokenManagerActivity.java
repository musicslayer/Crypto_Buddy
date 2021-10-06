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
import com.musicslayer.cryptobuddy.crash.CrashOnClickListener;
import com.musicslayer.cryptobuddy.crash.CrashOnDismissListener;
import com.musicslayer.cryptobuddy.crash.CrashOnShowListener;
import com.musicslayer.cryptobuddy.dialog.AddCustomTokenDialog;
import com.musicslayer.cryptobuddy.dialog.BaseDialogFragment;
import com.musicslayer.cryptobuddy.dialog.DownloadTokensDialog;
import com.musicslayer.cryptobuddy.dialog.ProgressDialog;
import com.musicslayer.cryptobuddy.dialog.ProgressDialogFragment;
import com.musicslayer.cryptobuddy.serialize.Serialization;
import com.musicslayer.cryptobuddy.util.ThrowableUtil;
import com.musicslayer.cryptobuddy.util.HelpUtil;
import com.musicslayer.cryptobuddy.util.RESTUtil;
import com.musicslayer.cryptobuddy.view.TokenManagerView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class TokenManagerActivity extends BaseActivity {
    public String tokenAllJSON = null;
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
        helpButton.setOnClickListener(new CrashOnClickListener(this) {
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
        addCustomTokenDialogFragment.setOnDismissListener(new CrashOnDismissListener(this) {
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
        addCustomTokenDialogFragment.restoreListeners(this, "add_custom_token");

        Button B_CustomToken = findViewById(R.id.token_manager_customTokenButton);
        B_CustomToken.setOnClickListener(new CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                addCustomTokenDialogFragment.show(TokenManagerActivity.this, "add_custom_token");
            }
        });

        ProgressDialogFragment progressFixedDialogFragment = ProgressDialogFragment.newInstance(ProgressDialog.class);
        progressFixedDialogFragment.setOnShowListener(new CrashOnShowListener(this) {
            @Override
            public void onShowImpl(DialogInterface dialog) {
                tokenAllJSON = RESTUtil.get("https://raw.githubusercontent.com/musicslayer/token_hub/main/token_info/ALL");
            }
        });
        progressFixedDialogFragment.setOnDismissListener(new CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(tokenAllJSON != null) {
                    JSONObject tokenAllJSONObject;
                    try {
                        tokenAllJSONObject = new JSONObject(tokenAllJSON);
                    }
                    catch(Exception e) {
                        ThrowableUtil.processThrowable(e);
                        throw new IllegalStateException(e);
                    }

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
                            throw new IllegalStateException(e);
                        }

                        tokenManagerView.tokenJSON = tokenTypeJSON.toString();
                        tokenManagerView.updateTokensFixed();
                    }
                }
            }
        });
        progressFixedDialogFragment.restoreListeners(this, "progress_fixed");

        ProgressDialogFragment progressDirectDialogFragment = ProgressDialogFragment.newInstance(ProgressDialog.class);
        progressDirectDialogFragment.setOnShowListener(new CrashOnShowListener(this) {
            @Override
            public void onShowImpl(DialogInterface dialog) {
                for(TokenManagerView tokenManagerView : tokenManagerViewArrayList) {
                    tokenManagerView.queryTokensDirect();
                }
            }
        });
        progressDirectDialogFragment.setOnDismissListener(new CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                for(TokenManagerView tokenManagerView : tokenManagerViewArrayList) {
                    tokenManagerView.updateTokensDirect();
                }
            }
        });
        progressDirectDialogFragment.restoreListeners(this, "progress_direct");

        BaseDialogFragment downloadTokensDialogFragment = BaseDialogFragment.newInstance(DownloadTokensDialog.class, "All");
        downloadTokensDialogFragment.setOnDismissListener(new CrashOnDismissListener(this) {
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
        B_MassUpdate.setOnClickListener(new CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                downloadTokensDialogFragment.show(TokenManagerActivity.this, "download");
            }
        });
    }

    @Override
    public void onSaveInstanceStateImpl(@NonNull Bundle bundle) {
        for(TokenManagerView tokenManagerView : tokenManagerViewArrayList) {
            bundle.putString("choice_" + tokenManagerView.tokenManager.getTokenType(), Serialization.string_serialize(tokenManagerView.choice));
        }
    }

    @Override
    public void onRestoreInstanceStateImpl(Bundle bundle) {
        if(bundle != null) {
            for(TokenManagerView tokenManagerView : tokenManagerViewArrayList) {
                tokenManagerView.choice = Serialization.string_deserialize(bundle.getString("choice_" + tokenManagerView.tokenManager.getTokenType()));
            }
        }
    }
}