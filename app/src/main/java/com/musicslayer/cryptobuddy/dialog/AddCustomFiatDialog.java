package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.asset.fiat.Fiat;
import com.musicslayer.cryptobuddy.asset.fiat.Fiat_Impl;
import com.musicslayer.cryptobuddy.asset.fiatmanager.FiatManager;
import com.musicslayer.cryptobuddy.crash.CrashDialogInterface;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.persistence.FiatManagerList;
import com.musicslayer.cryptobuddy.util.HelpUtil;
import com.musicslayer.cryptobuddy.util.ToastUtil;
import com.musicslayer.cryptobuddy.view.red.Int2EditText;
import com.musicslayer.cryptobuddy.view.red.PlainTextEditText;

import java.math.BigInteger;

public class AddCustomFiatDialog extends BaseDialog {
    public FiatManager chosenFiatManager = FiatManager.getFiatManagerFromKey("BaseFiatManager");

    public AddCustomFiatDialog(Activity activity) {
        super(activity);
    }

    public int getBaseViewID() {
        return R.id.add_custom_fiat_dialog;
    }

    public void createLayout(Bundle savedInstanceState) {
        setContentView(R.layout.dialog_add_custom_fiat);

        ImageButton helpButton = findViewById(R.id.add_custom_fiat_dialog_helpButton);
        helpButton.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            @Override
            public void onClickImpl(View view) {
                HelpUtil.showHelp(AddCustomFiatDialog.this.activity, R.raw.help_add_custom_fiat);
            }
        });

        PlainTextEditText E_NAME = findViewById(R.id.add_custom_fiat_dialog_nameEditText);
        PlainTextEditText E_SYMBOL = findViewById(R.id.add_custom_fiat_dialog_symbolEditText);
        Int2EditText E_DECIMALS = findViewById(R.id.add_custom_fiat_dialog_decimalsEditText);

        OnDismissListener replaceCustomFiatDialogFragmentListener = new CrashDialogInterface.CrashOnDismissListener(this.activity) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((ReplaceCustomFiatDialog)dialog).isComplete) {
                    chosenFiatManager.addCustomFiat(((ReplaceCustomFiatDialog)dialog).newFiat);
                    FiatManagerList.updateFiatManager(activity, chosenFiatManager);

                    ToastUtil.showToast(activity,"custom_fiat_added");
                    isComplete = true;
                    dismiss();
                }
            }
        };

        Button B_Confirm = findViewById(R.id.add_custom_fiat_dialog_confirmButton);
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

                    Fiat oldFiat = chosenFiatManager.custom_fiat_map.get(key);
                    Fiat newFiat = new Fiat_Impl(key, name, display_name, scale);

                    if(oldFiat == null) {
                        chosenFiatManager.addCustomFiat(newFiat);
                        FiatManagerList.updateFiatManager(activity, chosenFiatManager);

                        ToastUtil.showToast(activity,"custom_fiat_added");
                        isComplete = true;
                        dismiss();
                    }
                    else {
                        // Fiat already exists, so ask user if they want to override it.
                        BaseDialogFragment replaceCustomFiatDialogFragment = BaseDialogFragment.newInstance(ReplaceCustomFiatDialog.class, oldFiat, newFiat);
                        replaceCustomFiatDialogFragment.setOnDismissListener(replaceCustomFiatDialogFragmentListener);
                        replaceCustomFiatDialogFragment.show(AddCustomFiatDialog.this.activity, "replace_custom_fiat");
                    }
                }
            }
        });

        BaseDialogFragment replaceCustomFiatDialogFragment2 = (BaseDialogFragment) this.activity.getSupportFragmentManager().findFragmentByTag("replace_custom_fiat");
        if (replaceCustomFiatDialogFragment2 != null) {
            replaceCustomFiatDialogFragment2.setOnDismissListener(replaceCustomFiatDialogFragmentListener);
        }
    }
}
