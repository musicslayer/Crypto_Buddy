package com.musicslayer.cryptobuddy.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TableLayout;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.asset.tokenmanager.TokenManager;
import com.musicslayer.cryptobuddy.dialog.AddCustomTokenDialog;
import com.musicslayer.cryptobuddy.dialog.BaseDialogFragment;
import com.musicslayer.cryptobuddy.dialog.ProgressDialog;
import com.musicslayer.cryptobuddy.dialog.ProgressDialogFragment;
import com.musicslayer.cryptobuddy.util.Help;
import com.musicslayer.cryptobuddy.view.TokenManagerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class TokenManagerActivity extends BaseActivity {
    public int getAdLayoutViewID() {
        return R.id.token_manager_adLayout;
    }

    @Override
    public void onBackPressed() {
        finish();
        startActivity(new Intent(this, MainActivity.class));
    }

    public void createLayout () {
        setContentView(R.layout.activity_token_manager);

        ImageButton helpButton = findViewById(R.id.token_manager_helpButton);
        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
        addCustomTokenDialogFragment.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
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
        B_CustomToken.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addCustomTokenDialogFragment.show(TokenManagerActivity.this, "add_custom_token");
            }
        });

        ProgressDialogFragment progressDialogFragment = ProgressDialogFragment.newInstance(ProgressDialog.class);
        progressDialogFragment.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                for(TokenManagerView tokenManagerView : tokenManagerViewArrayList) {
                    tokenManagerView.queryTokens(TokenManagerActivity.this);
                }
            }
        });
        progressDialogFragment.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                for(TokenManagerView tokenManagerView : tokenManagerViewArrayList) {
                    tokenManagerView.updateTokens(TokenManagerActivity.this);
                }
            }
        });
        progressDialogFragment.restoreListeners(this, "progress");

        Button B_MassUpdate = findViewById(R.id.token_manager_massUpdateButton);
        B_MassUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialogFragment.show(TokenManagerActivity.this, "progress");
            }
        });
    }
}