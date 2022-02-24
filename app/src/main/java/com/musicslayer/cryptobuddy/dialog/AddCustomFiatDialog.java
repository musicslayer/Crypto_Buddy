package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.widget.Toolbar;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.asset.fiat.Fiat;
import com.musicslayer.cryptobuddy.asset.fiatmanager.FiatManager;
import com.musicslayer.cryptobuddy.crash.CrashDialogInterface;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.data.persistent.app.FiatManagerList;
import com.musicslayer.cryptobuddy.data.persistent.app.PersistentAppDataStore;
import com.musicslayer.cryptobuddy.util.HelpUtil;
import com.musicslayer.cryptobuddy.util.ToastUtil;
import com.musicslayer.cryptobuddy.view.red.Int2EditText;
import com.musicslayer.cryptobuddy.view.red.PlainTextEditText;

import java.math.BigInteger;

public class AddCustomFiatDialog extends BaseDialog {
    public String fiatType;

    public AddCustomFiatDialog(Activity activity, String fiatType) {
        super(activity);
        this.fiatType = fiatType;
    }

    public int getBaseViewID() {
        return R.id.add_custom_fiat_dialog;
    }

    public void createLayout(Bundle savedInstanceState) {
        setContentView(R.layout.dialog_add_custom_fiat);

        Toolbar toolbar = findViewById(R.id.add_custom_fiat_dialog_toolbar);
        // TODO Use Subtitle instead to save space.
        toolbar.setTitle("Add Custom " + fiatType + " Fiat");

        ImageButton helpButton = findViewById(R.id.add_custom_fiat_dialog_helpButton);
        helpButton.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            @Override
            public void onClickImpl(View view) {
                HelpUtil.showHelp(AddCustomFiatDialog.this.activity, R.raw.help_add_custom_fiat);
            }
        });

        FiatManager fiatManager = FiatManager.getFiatManagerFromFiatType(fiatType);

        PlainTextEditText E_NAME = findViewById(R.id.add_custom_fiat_dialog_nameEditText);
        PlainTextEditText E_SYMBOL = findViewById(R.id.add_custom_fiat_dialog_symbolEditText);
        Int2EditText E_DECIMALS = findViewById(R.id.add_custom_fiat_dialog_decimalsEditText);

        Fiat dummyFiat = new Fiat("", "", "", 0, "", null);
        BaseDialogFragment replaceCustomFiatDialogFragment = BaseDialogFragment.newInstance(ReplaceCustomFiatDialog.class, dummyFiat, dummyFiat);
        replaceCustomFiatDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this.activity) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((ReplaceCustomFiatDialog)dialog).isComplete) {
                    fiatManager.addCustomFiat(((ReplaceCustomFiatDialog)dialog).newFiat);
                    PersistentAppDataStore.getInstance(FiatManagerList.class).updateFiatManager(fiatManager);

                    ToastUtil.showToast("custom_fiat_added");
                    isComplete = true;
                    dismiss();
                }
            }
        });
        replaceCustomFiatDialogFragment.restoreListeners(this.activity, "replace_custom_fiat");

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

                    Fiat oldFiat = fiatManager.custom_fiat_map.get(key);
                    Fiat newFiat = Fiat.buildFiat(key, name, display_name, scale, fiatManager.getFiatType());

                    if(oldFiat == null) {
                        fiatManager.addCustomFiat(newFiat);
                        PersistentAppDataStore.getInstance(FiatManagerList.class).updateFiatManager(fiatManager);

                        ToastUtil.showToast("custom_fiat_added");
                        isComplete = true;
                        dismiss();
                    }
                    else {
                        // Fiat already exists, so ask user if they want to override it.
                        replaceCustomFiatDialogFragment.updateArguments(ReplaceCustomFiatDialog.class, oldFiat, newFiat);
                        replaceCustomFiatDialogFragment.show(AddCustomFiatDialog.this.activity, "replace_custom_fiat");
                    }
                }
            }
        });
    }
}
