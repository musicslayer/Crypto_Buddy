package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.widget.Toolbar;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.asset.Asset;
import com.musicslayer.cryptobuddy.asset.crypto.token.Token;
import com.musicslayer.cryptobuddy.asset.tokenmanager.TokenManager;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.view.asset.SelectAndSearchView;
import com.musicslayer.cryptobuddy.view.asset.TokenView;

import java.util.ArrayList;

public class ViewTokensDialog extends BaseDialog {
    public String tokenType;
    public boolean canGetJSON;

    int LAST_CHECK = 0;

    public ViewTokensDialog(Activity activity, String tokenType, Boolean canGetJSON) {
        super(activity);
        this.tokenType = tokenType;
        this.canGetJSON = canGetJSON;
    }

    public int getBaseViewID() {
        return R.id.view_tokens_dialog;
    }

    public void createLayout(Bundle savedInstanceState) {
        setContentView(R.layout.dialog_view_tokens);

        Toolbar toolbar = findViewById(R.id.view_tokens_dialog_toolbar);
        toolbar.setTitle("View " + tokenType + " Tokens");

        RadioGroup radioGroup = findViewById(R.id.view_tokens_dialog_radioGroup);
        RadioButton[] rb = new RadioButton[3];

        rb[0] = findViewById(R.id.view_tokens_dialog_downloadedRadioButton);
        rb[0].setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                LAST_CHECK = 0;
                updateLayout();
            }
        });

        rb[1] = findViewById(R.id.view_tokens_dialog_foundRadioButton);
        rb[1].setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                LAST_CHECK = 1;
                updateLayout();
            }
        });

        rb[2] = findViewById(R.id.view_tokens_dialog_customRadioButton);
        rb[2].setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                LAST_CHECK = 2;
                updateLayout();
            }
        });

        radioGroup.check(rb[LAST_CHECK].getId());
        rb[LAST_CHECK].callOnClick();

        updateLayout();
    }

    public void updateLayout() {
        TokenView tokenView = findViewById(R.id.view_tokens_dialog_tokenView);

        SelectAndSearchView ssv = findViewById(R.id.view_tokens_dialog_selectAndSearchView);
        ssv.setChooseAssetListener(new SelectAndSearchView.ChooseAssetListener() {
            @Override
            public void onAssetChosen(Asset asset) {
                tokenView.setToken((Token)asset);
            }
        });

        ssv.setIncludesFiat(false);
        ssv.setIncludesCoin(false);
        ssv.setIncludesToken(true);

        TokenManager tokenManager = TokenManager.getTokenManagerFromTokenType(tokenType);

        if(LAST_CHECK == 0) {
            ssv.setTokenOptions(tokenManager.downloaded_tokens);
        }
        else if(LAST_CHECK == 1) {
            ssv.setTokenOptions(tokenManager.found_tokens);
        }
        else if(LAST_CHECK == 2) {
            ssv.setTokenOptions(tokenManager.custom_tokens);
        }

        ArrayList<TokenManager> tokenManagerArrayList = new ArrayList<>();
        tokenManagerArrayList.add(tokenManager);
        ssv.setTokenManagerOptions(tokenManagerArrayList);

        ssv.chooseToken(tokenType);
    }

    @Override
    public Bundle onSaveInstanceStateImpl(Bundle bundle) {
        bundle.putInt("lastcheck", LAST_CHECK);
        return bundle;
    }

    @Override
    public void onRestoreInstanceStateImpl(Bundle bundle) {
        if(bundle != null) {
            LAST_CHECK = bundle.getInt("lastcheck");
        }
    }
}
