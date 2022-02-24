package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.widget.Toolbar;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.asset.coinmanager.CoinManager;
import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;
import com.musicslayer.cryptobuddy.crash.CrashDialogInterface;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.data.persistent.app.CoinManagerList;
import com.musicslayer.cryptobuddy.data.persistent.app.PersistentAppDataStore;
import com.musicslayer.cryptobuddy.util.HelpUtil;
import com.musicslayer.cryptobuddy.util.ToastUtil;
import com.musicslayer.cryptobuddy.view.red.Int2EditText;
import com.musicslayer.cryptobuddy.view.red.PlainTextEditText;

import java.math.BigInteger;

public class AddCustomCoinDialog extends BaseDialog {
    public String coinType;

    public AddCustomCoinDialog(Activity activity, String coinType) {
        super(activity);
        this.coinType = coinType;
    }

    public int getBaseViewID() {
        return R.id.add_custom_coin_dialog;
    }

    public void createLayout(Bundle savedInstanceState) {
        setContentView(R.layout.dialog_add_custom_coin);

        Toolbar toolbar = findViewById(R.id.add_custom_coin_dialog_toolbar);
        toolbar.setSubtitle("Type = " + coinType);

        ImageButton helpButton = findViewById(R.id.add_custom_coin_dialog_helpButton);
        helpButton.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            @Override
            public void onClickImpl(View view) {
                HelpUtil.showHelp(AddCustomCoinDialog.this.activity, R.raw.help_add_custom_coin);
            }
        });

        CoinManager coinManager = CoinManager.getCoinManagerFromCoinType(coinType);

        PlainTextEditText E_NAME = findViewById(R.id.add_custom_coin_dialog_nameEditText);
        PlainTextEditText E_SYMBOL = findViewById(R.id.add_custom_coin_dialog_symbolEditText);
        Int2EditText E_DECIMALS = findViewById(R.id.add_custom_coin_dialog_decimalsEditText);

        Coin dummyCoin = new Coin("", "", "", 0, "", null);
        BaseDialogFragment replaceCustomCoinDialogFragment = BaseDialogFragment.newInstance(ReplaceCustomCoinDialog.class, dummyCoin, dummyCoin);
        replaceCustomCoinDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this.activity) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((ReplaceCustomCoinDialog)dialog).isComplete) {
                    coinManager.addCustomCoin(((ReplaceCustomCoinDialog)dialog).newCoin);
                    PersistentAppDataStore.getInstance(CoinManagerList.class).updateCoinManager(coinManager);

                    ToastUtil.showToast("custom_coin_added");
                    isComplete = true;
                    dismiss();
                }
            }
        });
        replaceCustomCoinDialogFragment.restoreListeners(this.activity, "replace_custom_coin");

        Button B_Confirm = findViewById(R.id.add_custom_coin_dialog_confirmButton);
        B_Confirm.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            @Override
            public void onClickImpl(View view) {
                // Don't short circuit - we need to test everything.
                boolean isValid = E_NAME.test() & E_SYMBOL.test() & E_DECIMALS.test();

                if(!isValid) {
                    ToastUtil.showToast("must_fill_inputs");
                }
                else {
                    String name = E_SYMBOL.getTextString();
                    String display_name = E_NAME.getTextString();
                    int scale = new BigInteger(E_DECIMALS.getTextString()).intValue();

                    String key = name;

                    // For custom coin, use an invalid ID.
                    String id = "?";

                    Coin oldCoin = coinManager.custom_coin_map.get(key);
                    Coin newCoin = Coin.buildCoin(key, name, display_name, scale, coinManager.getCoinType(), id);

                    if(oldCoin == null) {
                        coinManager.addCustomCoin(newCoin);
                        PersistentAppDataStore.getInstance(CoinManagerList.class).updateCoinManager(coinManager);

                        ToastUtil.showToast("custom_coin_added");
                        isComplete = true;
                        dismiss();
                    }
                    else {
                        // Coin already exists, so ask user if they want to override it.
                        replaceCustomCoinDialogFragment.updateArguments(ReplaceCustomCoinDialog.class, oldCoin, newCoin);
                        replaceCustomCoinDialogFragment.show(AddCustomCoinDialog.this.activity, "replace_custom_coin");
                    }
                }
            }
        });
    }
}
