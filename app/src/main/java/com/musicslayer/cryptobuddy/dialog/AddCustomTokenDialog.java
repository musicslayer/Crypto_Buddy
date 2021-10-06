package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.asset.crypto.token.Token;
import com.musicslayer.cryptobuddy.asset.tokenmanager.TokenManager;
import com.musicslayer.cryptobuddy.crash.CrashAdapterView;
import com.musicslayer.cryptobuddy.crash.CrashDialogInterface;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.util.HelpUtil;
import com.musicslayer.cryptobuddy.util.ToastUtil;
import com.musicslayer.cryptobuddy.view.BorderedSpinnerView;
import com.musicslayer.cryptobuddy.view.red.Int2EditText;
import com.musicslayer.cryptobuddy.view.red.PlainTextEditText;

import java.math.BigInteger;

public class AddCustomTokenDialog extends BaseDialog {
    public TokenManager chosenTokenManager;

    public AddCustomTokenDialog(Activity activity) {
        super(activity);
    }

    public int getBaseViewID() {
        return R.id.add_custom_token_dialog;
    }

    public void createLayout() {
        setContentView(R.layout.dialog_add_custom_token);

        ImageButton helpButton = findViewById(R.id.add_custom_token_dialog_helpButton);
        helpButton.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            @Override
            public void onClickImpl(View view) {
                HelpUtil.showHelp(AddCustomTokenDialog.this.activity, R.raw.help_add_custom_token);
            }
        });

        BorderedSpinnerView bsv = findViewById(R.id.add_custom_token_dialog_tokenTypeSpinner);
        bsv.setOptions(TokenManager.tokenManagers_token_types);
        bsv.setOnItemSelectedListener(new CrashAdapterView.CrashOnItemSelectedListener(this.activity) {
            public void onNothingSelectedImpl(AdapterView<?> parent){}
            public void onItemSelectedImpl(AdapterView<?> parent, View view, int pos, long id) {
                chosenTokenManager = TokenManager.getTokenManagerFromTokenType(TokenManager.tokenManagers_token_types.get(pos));
            }
        });

        PlainTextEditText E_ID = findViewById(R.id.add_custom_token_dialog_idEditText); // i.e. the contract address
        PlainTextEditText E_NAME = findViewById(R.id.add_custom_token_dialog_nameEditText);
        PlainTextEditText E_SYMBOL = findViewById(R.id.add_custom_token_dialog_symbolEditText);
        Int2EditText E_DECIMALS = findViewById(R.id.add_custom_token_dialog_decimalsEditText);

        DialogInterface.OnDismissListener replaceCustomTokenDialogFragmentListener = new CrashDialogInterface.CrashOnDismissListener(this.activity) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((ReplaceCustomTokenDialog)dialog).isComplete) {
                    chosenTokenManager.addCustomToken(((ReplaceCustomTokenDialog)dialog).newToken);
                    chosenTokenManager.save(activity, "custom");

                    ToastUtil.showToast(activity,"custom_token_added");
                    isComplete = true;
                    dismiss();
                }
            }
        };

        Button B_Confirm = findViewById(R.id.add_custom_token_dialog_confirmButton);
        B_Confirm.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            @Override
            public void onClickImpl(View view) {
                // Don't short circuit - we need to test everything.
                boolean isValid = E_ID.test() & E_NAME.test() & E_SYMBOL.test() & E_DECIMALS.test();

                if(isValid) {
                    String key = E_ID.getText().toString();
                    String name = E_SYMBOL.getText().toString();
                    String display_name = E_NAME.getText().toString();
                    int scale = new BigInteger(E_DECIMALS.getText().toString()).intValue();
                    String id = key;

                    Token oldToken = chosenTokenManager.custom_token_map.get(key);
                    Token newToken = new Token(key, name, display_name, scale, id, chosenTokenManager.getBlockchainID(), chosenTokenManager.getTokenType());
                    if(oldToken == null) {
                        chosenTokenManager.addCustomToken(newToken);
                        chosenTokenManager.save(activity, "custom");

                        ToastUtil.showToast(activity,"custom_token_added");
                        isComplete = true;
                        dismiss();
                    }
                    else {
                        // Token already exists, so ask user if they want to override it.
                        BaseDialogFragment replaceCustomTokenDialogFragment = BaseDialogFragment.newInstance(ReplaceCustomTokenDialog.class, oldToken, newToken);
                        replaceCustomTokenDialogFragment.setOnDismissListener(replaceCustomTokenDialogFragmentListener);
                        replaceCustomTokenDialogFragment.show(AddCustomTokenDialog.this.activity, "replace_custom_token");
                    }
                }
            }
        });

        BaseDialogFragment replaceCustomTokenDialogFragment2 = (BaseDialogFragment) this.activity.getSupportFragmentManager().findFragmentByTag("replace_custom_token");
        if (replaceCustomTokenDialogFragment2 != null) {
            replaceCustomTokenDialogFragment2.setOnDismissListener(replaceCustomTokenDialogFragmentListener);
        }
    }
}
