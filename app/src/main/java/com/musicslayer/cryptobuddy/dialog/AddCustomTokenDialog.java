package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.widget.Toolbar;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.asset.crypto.token.Token;
import com.musicslayer.cryptobuddy.asset.tokenmanager.TokenManager;
import com.musicslayer.cryptobuddy.crash.CrashDialogInterface;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.data.persistent.app.PersistentAppDataStore;
import com.musicslayer.cryptobuddy.data.persistent.app.TokenManagerList;
import com.musicslayer.cryptobuddy.util.HelpUtil;
import com.musicslayer.cryptobuddy.util.ToastUtil;
import com.musicslayer.cryptobuddy.view.red.Int2EditText;
import com.musicslayer.cryptobuddy.view.red.PlainTextEditText;

import java.math.BigInteger;

public class AddCustomTokenDialog extends BaseDialog {
    public String tokenType;

    public AddCustomTokenDialog(Activity activity, String tokenType) {
        super(activity);
        this.tokenType = tokenType;
    }

    public int getBaseViewID() {
        return R.id.add_custom_token_dialog;
    }

    public void createLayout(Bundle savedInstanceState) {
        setContentView(R.layout.dialog_add_custom_token);

        Toolbar toolbar = findViewById(R.id.add_custom_token_dialog_toolbar);
        toolbar.setTitle("Add Custom " + tokenType + " Token");

        ImageButton helpButton = findViewById(R.id.add_custom_token_dialog_helpButton);
        helpButton.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            @Override
            public void onClickImpl(View view) {
                HelpUtil.showHelp(AddCustomTokenDialog.this.activity, R.raw.help_add_custom_token);
            }
        });

        TokenManager tokenManager = TokenManager.getTokenManagerFromTokenType(tokenType);

        PlainTextEditText E_ID = findViewById(R.id.add_custom_token_dialog_idEditText); // i.e. the contract address
        PlainTextEditText E_NAME = findViewById(R.id.add_custom_token_dialog_nameEditText);
        PlainTextEditText E_SYMBOL = findViewById(R.id.add_custom_token_dialog_symbolEditText);
        Int2EditText E_DECIMALS = findViewById(R.id.add_custom_token_dialog_decimalsEditText);

        Token dummyToken = new Token("", "", "", 0, "", null);
        BaseDialogFragment replaceCustomTokenDialogFragment = BaseDialogFragment.newInstance(ReplaceCustomTokenDialog.class, dummyToken, dummyToken);
        replaceCustomTokenDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this.activity) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((ReplaceCustomTokenDialog)dialog).isComplete) {
                    tokenManager.addCustomToken(((ReplaceCustomTokenDialog)dialog).newToken);
                    PersistentAppDataStore.getInstance(TokenManagerList.class).updateTokenManager(tokenManager);

                    ToastUtil.showToast("custom_token_added");
                    isComplete = true;
                    dismiss();
                }
            }
        });
        replaceCustomTokenDialogFragment.restoreListeners(this.activity, "replace_custom_token");

        Button B_Confirm = findViewById(R.id.add_custom_token_dialog_confirmButton);
        B_Confirm.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            @Override
            public void onClickImpl(View view) {
                // Don't short circuit - we need to test everything.
                boolean isValid = E_ID.test() & E_NAME.test() & E_SYMBOL.test() & E_DECIMALS.test();

                if(!isValid) {
                    ToastUtil.showToast("must_fill_inputs");
                }
                else {
                    String key = E_ID.getTextString();
                    String name = E_SYMBOL.getTextString();
                    String display_name = E_NAME.getTextString();
                    int scale = new BigInteger(E_DECIMALS.getTextString()).intValue();
                    String id = key;

                    Token oldToken = tokenManager.custom_token_map.get(key);
                    Token newToken = Token.buildToken(key, name, display_name, scale, tokenManager.getTokenType(), id, tokenManager.getCoinGeckoBlockchainID());

                    if(oldToken == null) {
                        tokenManager.addCustomToken(newToken);
                        PersistentAppDataStore.getInstance(TokenManagerList.class).updateTokenManager(tokenManager);

                        ToastUtil.showToast("custom_token_added");
                        isComplete = true;
                        dismiss();
                    }
                    else {
                        // Token already exists, so ask user if they want to override it.
                        replaceCustomTokenDialogFragment.updateArguments(ReplaceCustomTokenDialog.class, oldToken, newToken);
                        replaceCustomTokenDialogFragment.show(AddCustomTokenDialog.this.activity, "replace_custom_token");
                    }
                }
            }
        });
    }
}
