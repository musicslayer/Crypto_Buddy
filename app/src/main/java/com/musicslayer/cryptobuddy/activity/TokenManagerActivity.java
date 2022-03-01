package com.musicslayer.cryptobuddy.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.asset.tokenmanager.TokenManager;
import com.musicslayer.cryptobuddy.crash.CrashDialogInterface;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.data.bridge.DataBridge;
import com.musicslayer.cryptobuddy.data.persistent.app.PersistentAppDataStore;
import com.musicslayer.cryptobuddy.dialog.BaseDialogFragment;
import com.musicslayer.cryptobuddy.dialog.ConfirmDeleteTokensDialog;
import com.musicslayer.cryptobuddy.dialog.DeleteTokensDialog;
import com.musicslayer.cryptobuddy.dialog.DownloadTokensDialog;
import com.musicslayer.cryptobuddy.dialog.ProgressDialog;
import com.musicslayer.cryptobuddy.dialog.ProgressDialogFragment;
import com.musicslayer.cryptobuddy.data.persistent.app.TokenManagerList;
import com.musicslayer.cryptobuddy.util.ThrowableUtil;
import com.musicslayer.cryptobuddy.util.HelpUtil;
import com.musicslayer.cryptobuddy.util.WebUtil;
import com.musicslayer.cryptobuddy.util.ToastUtil;
import com.musicslayer.cryptobuddy.view.asset.TokenManagerView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

// TODO Tokens that cannot be downloaded.
//  Find Custom Token option.
//  Paste in contract, confirm details.

public class TokenManagerActivity extends BaseActivity {
    ArrayList<TokenManagerView> tokenManagerViewArrayList;
    public ArrayList<String> choices;

    @Override
    public int getAdLayoutViewID() {
        return R.id.token_manager_adLayout;
    }

    @Override
    public int getProgressViewID() {
        return R.id.token_manager_progressBar;
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

        BaseDialogFragment confirmDeleteTokensDialogFragment = BaseDialogFragment.newInstance(ConfirmDeleteTokensDialog.class, "All");
        confirmDeleteTokensDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(TokenManagerActivity.this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((ConfirmDeleteTokensDialog)dialog).isComplete) {
                    if(choices.contains("downloaded")) {
                        TokenManager.resetAllDownloadedTokens();
                    }
                    if(choices.contains("found")) {
                        TokenManager.resetAllFoundTokens();
                    }
                    if(choices.contains("custom")) {
                        TokenManager.resetAllCustomTokens();
                    }

                    PersistentAppDataStore.getInstance(TokenManagerList.class).saveAllData();
                    ToastUtil.showToast("reset_tokens");

                    updateLayout();
                }
            }
        });
        confirmDeleteTokensDialogFragment.restoreListeners(this, "confirm_delete_all_tokens");

        BaseDialogFragment deleteTokensDialogFragment = BaseDialogFragment.newInstance(DeleteTokensDialog.class, "All", true);
        deleteTokensDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(TokenManagerActivity.this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((DeleteTokensDialog)dialog).isComplete) {
                    choices = ((DeleteTokensDialog)dialog).user_CHOICES;
                    confirmDeleteTokensDialogFragment.show(TokenManagerActivity.this, "confirm_delete_all_tokens");
                }
            }
        });
        deleteTokensDialogFragment.restoreListeners(TokenManagerActivity.this, "delete_all_tokens");

        AppCompatButton B_DELETE = findViewById(R.id.token_manager_deleteAllTokensButton);
        B_DELETE.setOnClickListener(new CrashView.CrashOnClickListener(TokenManagerActivity.this) {
            public void onClickImpl(View v) {
                deleteTokensDialogFragment.show(TokenManagerActivity.this, "delete_all_tokens");
            }
        });

        ProgressDialogFragment progressFixedDialogFragment = ProgressDialogFragment.newInstance(ProgressDialog.class);
        progressFixedDialogFragment.setOnShowListener(new CrashDialogInterface.CrashOnShowListener(this) {
            @Override
            public void onShowImpl(DialogInterface dialog) {
                ProgressDialogFragment.updateProgressTitle("Downloading All Tokens...");

                String tokenAllJSON = WebUtil.get("https://raw.githubusercontent.com/musicslayer/token_hub/main/token_info/ALL");
                ProgressDialogFragment.setValue(DataBridge.serializeValue(tokenAllJSON, String.class));
            }
        });
        progressFixedDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                String tokenAllJSON = DataBridge.deserializeValue(ProgressDialogFragment.getValue(), String.class);

                if(tokenAllJSON == null) {
                    ToastUtil.showToast("tokens_not_downloaded");
                }
                else {
                    JSONObject tokenAllJSONObject;
                    try {
                        tokenAllJSONObject = new JSONObject(tokenAllJSON);
                    }
                    catch(Exception e) {
                        ThrowableUtil.processThrowable(e);
                        ToastUtil.showToast("tokens_not_downloaded");
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
                        PersistentAppDataStore.getInstance(TokenManagerList.class).updateTokenManager(tokenManagerView.tokenManager);

                        tokenManagerView.updateLayout();
                    }

                    if(!isAllComplete) {
                        ToastUtil.showToast("tokens_not_downloaded");
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

                ProgressDialogFragment.setValue(DataBridge.serializeArrayList(tokenJSONArrayList, String.class));
            }
        });
        progressDirectDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                ArrayList<String> tokenJSONArrayList = DataBridge.deserializeArrayList(ProgressDialogFragment.getValue(), String.class);

                boolean isAllComplete = true;

                for(int i = 0; i < tokenManagerViewArrayList.size(); i++) {
                    TokenManagerView tokenManagerView = tokenManagerViewArrayList.get(i);

                    tokenManagerView.tokenManager.resetDownloadedTokens();
                    boolean isComplete = tokenManagerView.tokenManager.parse(tokenJSONArrayList.get(i));
                    PersistentAppDataStore.getInstance(TokenManagerList.class).updateTokenManager(tokenManagerView.tokenManager);

                    tokenManagerView.updateLayout();
                    if(!isComplete) {
                        isAllComplete = false;
                    }
                }

                if(!isAllComplete) {
                    ToastUtil.showToast("tokens_not_downloaded");
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

        Button B_DOWNLOAD = findViewById(R.id.token_manager_downloadAllTokensButton);
        B_DOWNLOAD.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                downloadTokensDialogFragment.show(TokenManagerActivity.this, "download");
            }
        });

        updateLayout();
    }

    public void updateLayout() {
        TableLayout tableLayout = findViewById(R.id.token_manager_tableLayout);
        TableRow firstRow = findViewById(R.id.token_manager_tableRow1);

        tableLayout.removeAllViews();
        tableLayout.addView(firstRow);

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
    }

    @Override
    public void onSaveInstanceStateImpl(@NonNull Bundle bundle) {
        super.onSaveInstanceStateImpl(bundle);
        for(TokenManagerView tokenManagerView : tokenManagerViewArrayList) {
            bundle.putParcelable("tokenManagerView_" + tokenManagerView.tokenManager.getTokenType(), tokenManagerView.onSaveInstanceState());
        }
        bundle.putStringArrayList("choices", choices);
    }

    @Override
    public void onRestoreInstanceStateImpl(Bundle bundle) {
        super.onRestoreInstanceStateImpl(bundle);
        if(bundle != null) {
            for(TokenManagerView tokenManagerView : tokenManagerViewArrayList) {
                tokenManagerView.onRestoreInstanceState(bundle.getParcelable("tokenManagerView_" + tokenManagerView.tokenManager.getTokenType()));
            }
            choices = bundle.getStringArrayList("choices");
        }
    }
}