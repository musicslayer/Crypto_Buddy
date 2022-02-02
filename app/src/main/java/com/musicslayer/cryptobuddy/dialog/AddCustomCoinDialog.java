package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.asset.coinmanager.CoinManager;
import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;
import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin_Impl;
import com.musicslayer.cryptobuddy.crash.CrashDialogInterface;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.persistence.CoinManagerList;
import com.musicslayer.cryptobuddy.util.HelpUtil;
import com.musicslayer.cryptobuddy.util.ToastUtil;
import com.musicslayer.cryptobuddy.view.red.Int2EditText;
import com.musicslayer.cryptobuddy.view.red.PlainTextEditText;

import java.math.BigInteger;

public class AddCustomCoinDialog extends BaseDialog {
    public CoinManager chosenCoinManager = CoinManager.getCoinManagerFromKey("BaseCoinManager");

    public AddCustomCoinDialog(Activity activity) {
        super(activity);
    }

    public int getBaseViewID() {
        return R.id.add_custom_coin_dialog;
    }

    public void createLayout(Bundle savedInstanceState) {
        setContentView(R.layout.dialog_add_custom_coin);

        ImageButton helpButton = findViewById(R.id.add_custom_coin_dialog_helpButton);
        helpButton.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            @Override
            public void onClickImpl(View view) {
                HelpUtil.showHelp(AddCustomCoinDialog.this.activity, R.raw.help_add_custom_coin);
            }
        });

        PlainTextEditText E_NAME = findViewById(R.id.add_custom_coin_dialog_nameEditText);
        PlainTextEditText E_SYMBOL = findViewById(R.id.add_custom_coin_dialog_symbolEditText);
        Int2EditText E_DECIMALS = findViewById(R.id.add_custom_coin_dialog_decimalsEditText);

        OnDismissListener replaceCustomCoinDialogFragmentListener = new CrashDialogInterface.CrashOnDismissListener(this.activity) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((ReplaceCustomCoinDialog)dialog).isComplete) {
                    chosenCoinManager.addCustomCoin(((ReplaceCustomCoinDialog)dialog).newCoin);
                    CoinManagerList.updateCoinManager(activity, chosenCoinManager);

                    ToastUtil.showToast(activity,"custom_coin_added");
                    isComplete = true;
                    dismiss();
                }
            }
        };

        Button B_Confirm = findViewById(R.id.add_custom_coin_dialog_confirmButton);
        B_Confirm.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            @Override
            public void onClickImpl(View view) {
                // Don't short circuit - we need to test everything.
                boolean isValid = E_NAME.test() & E_SYMBOL.test() & E_DECIMALS.test();

                if(isValid) {
                    String name = E_SYMBOL.getTextString();
                    String display_name = E_NAME.getTextString();
                    int scale = new BigInteger(E_DECIMALS.getTextString()).intValue();

                    String key = name;

                    // For custom coin, use an invalid ID.
                    String id = "?";

                    Coin oldCoin = chosenCoinManager.custom_coin_map.get(key);
                    Coin newCoin = new Coin_Impl(key, name, display_name, scale, id);

                    if(oldCoin == null) {
                        chosenCoinManager.addCustomCoin(newCoin);
                        CoinManagerList.updateCoinManager(activity, chosenCoinManager);

                        ToastUtil.showToast(activity,"custom_coin_added");
                        isComplete = true;
                        dismiss();
                    }
                    else {
                        // Coin already exists, so ask user if they want to override it.
                        BaseDialogFragment replaceCustomCoinDialogFragment = BaseDialogFragment.newInstance(ReplaceCustomCoinDialog.class, oldCoin, newCoin);
                        replaceCustomCoinDialogFragment.setOnDismissListener(replaceCustomCoinDialogFragmentListener);
                        replaceCustomCoinDialogFragment.show(AddCustomCoinDialog.this.activity, "replace_custom_coin");
                    }
                }
            }
        });

        BaseDialogFragment replaceCustomCoinDialogFragment2 = (BaseDialogFragment) this.activity.getSupportFragmentManager().findFragmentByTag("replace_custom_coin");
        if (replaceCustomCoinDialogFragment2 != null) {
            replaceCustomCoinDialogFragment2.setOnDismissListener(replaceCustomCoinDialogFragmentListener);
        }
    }
}
