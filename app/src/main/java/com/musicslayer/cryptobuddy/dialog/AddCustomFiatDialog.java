package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.asset.fiat.Fiat;
import com.musicslayer.cryptobuddy.asset.fiatmanager.FiatManager;
import com.musicslayer.cryptobuddy.crash.CrashAdapterView;
import com.musicslayer.cryptobuddy.crash.CrashDialogInterface;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.data.persistent.app.FiatManagerList;
import com.musicslayer.cryptobuddy.data.persistent.app.PersistentAppDataStore;
import com.musicslayer.cryptobuddy.util.HelpUtil;
import com.musicslayer.cryptobuddy.util.ToastUtil;
import com.musicslayer.cryptobuddy.view.BorderedSpinnerView;
import com.musicslayer.cryptobuddy.view.red.Int2EditText;
import com.musicslayer.cryptobuddy.view.red.PlainTextEditText;

import java.math.BigInteger;

public class AddCustomFiatDialog extends BaseDialog {
    public FiatManager chosenFiatManager;

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

        BorderedSpinnerView bsv = findViewById(R.id.add_custom_fiat_dialog_fiatTypeSpinner);
        bsv.setOptions(FiatManager.fiatManagers_fiat_types);
        bsv.setOnItemSelectedListener(new CrashAdapterView.CrashOnItemSelectedListener(this.activity) {
            public void onNothingSelectedImpl(AdapterView<?> parent){}
            public void onItemSelectedImpl(AdapterView<?> parent, View view, int pos, long id) {
                chosenFiatManager = FiatManager.getFiatManagerFromFiatType(FiatManager.fiatManagers_fiat_types.get(pos));
            }
        });

        if(FiatManager.fiatManagers_fiat_types.size() == 1) {
            bsv.setVisibility(View.GONE);
        }

        PlainTextEditText E_NAME = findViewById(R.id.add_custom_fiat_dialog_nameEditText);
        PlainTextEditText E_SYMBOL = findViewById(R.id.add_custom_fiat_dialog_symbolEditText);
        Int2EditText E_DECIMALS = findViewById(R.id.add_custom_fiat_dialog_decimalsEditText);

        OnDismissListener replaceCustomFiatDialogFragmentListener = new CrashDialogInterface.CrashOnDismissListener(this.activity) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((ReplaceCustomFiatDialog)dialog).isComplete) {
                    chosenFiatManager.addCustomFiat(((ReplaceCustomFiatDialog)dialog).newFiat);
                    PersistentAppDataStore.getInstance(FiatManagerList.class).updateFiatManager(chosenFiatManager);

                    ToastUtil.showToast("custom_fiat_added");
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

                if(!isValid) {
                    ToastUtil.showToast("must_fill_inputs");
                }
                else {
                    String name = E_SYMBOL.getTextString();
                    String display_name = E_NAME.getTextString();
                    int scale = new BigInteger(E_DECIMALS.getTextString()).intValue();

                    String key = name;

                    Fiat oldFiat = chosenFiatManager.custom_fiat_map.get(key);
                    Fiat newFiat = Fiat.buildFiat(key, name, display_name, scale, chosenFiatManager.getFiatType());

                    if(oldFiat == null) {
                        chosenFiatManager.addCustomFiat(newFiat);
                        PersistentAppDataStore.getInstance(FiatManagerList.class).updateFiatManager(chosenFiatManager);

                        ToastUtil.showToast("custom_fiat_added");
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
