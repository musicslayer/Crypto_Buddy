package com.musicslayer.cryptobuddy.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TableLayout;

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
import com.musicslayer.cryptobuddy.util.ThrowableLogger;
import com.musicslayer.cryptobuddy.util.Help;
import com.musicslayer.cryptobuddy.util.REST;
import com.musicslayer.cryptobuddy.view.TokenManagerView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class TokenManagerActivity extends BaseActivity {
    public String tokenAllJSON = null;

    public int getAdLayoutViewID() {
        return R.id.token_manager_adLayout;
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    public void createLayout () {
        setContentView(R.layout.activity_token_manager);

        ImageButton helpButton = findViewById(R.id.token_manager_helpButton);
        helpButton.setOnClickListener(new CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                Help.showHelp(TokenManagerActivity.this, R.raw.help_token_manager);
            }
        });

        TableLayout tableLayout = findViewById(R.id.token_manager_tableLayout);

        ArrayList<String> tokenTypes = TokenManager.tokenManagers_token_types;
        Collections.sort(tokenTypes, new Comparator<String>() {
            @Override
            public int compare(String a, String b) {
                return a.toLowerCase().compareTo(b.toLowerCase());
            }
        });

        ArrayList<TokenManagerView> tokenManagerViewArrayList = new ArrayList<>();
        for(String tokenType : tokenTypes) {
            TokenManager tokenManager = TokenManager.getTokenManagerFromTokenType(tokenType);
            TokenManagerView tokenManagerView = new TokenManagerView(TokenManagerActivity.this, tokenManager);
            tokenManagerView.updateLayout(TokenManagerActivity.this);

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
                        tokenManagerView.updateLayout(TokenManagerActivity.this);
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
                tokenAllJSON = REST.get("https://raw.githubusercontent.com/musicslayer/token_hub/main/token_info/ALL");
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
                        ThrowableLogger.processThrowable(e);
                        return;
                    }

                    // Try each individually.
                    for(TokenManagerView tokenManagerView : tokenManagerViewArrayList) {
                        String settingsKey = tokenManagerView.tokenManager.getSettingsKey();

                        if(!tokenAllJSONObject.has(settingsKey)) {
                            continue;
                        }

                        JSONObject tokenTypeJSON;
                        try {
                            tokenTypeJSON = tokenAllJSONObject.getJSONObject(settingsKey);
                        }
                        catch(Exception e) {
                            ThrowableLogger.processThrowable(e);
                            continue;
                        }

                        tokenManagerView.tokenJSON = tokenTypeJSON.toString();
                        tokenManagerView.updateTokensFixed(TokenManagerActivity.this);
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
                    tokenManagerView.queryTokensDirect(TokenManagerActivity.this);
                }
            }
        });
        progressDirectDialogFragment.setOnDismissListener(new CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                for(TokenManagerView tokenManagerView : tokenManagerViewArrayList) {
                    tokenManagerView.updateTokensDirect(TokenManagerActivity.this);
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
}