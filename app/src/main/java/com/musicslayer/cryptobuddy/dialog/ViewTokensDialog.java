package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.asset.tokenmanager.TokenManager;
import com.musicslayer.cryptobuddy.view.SelectAndSearchView;

import java.util.ArrayList;

public class ViewTokensDialog extends BaseDialog {
    public String tokenType;
    public boolean canGetJSON;

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

        SelectAndSearchView ssv = findViewById(R.id.view_tokens_dialog_selectAndSearchView);
        ssv.setIncludesFiat(false);
        ssv.setIncludesCoin(false);
        ssv.setIncludesToken(true);

        ArrayList<TokenManager> tokenManagerArrayList = new ArrayList<>();
        tokenManagerArrayList.add(TokenManager.getTokenManagerFromTokenType(tokenType));
        ssv.setTokenManagerOptions(tokenManagerArrayList);

        ssv.chooseToken(tokenType);

        updateLayout();
    }

    public void updateLayout() {
    }
}
