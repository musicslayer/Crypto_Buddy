package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.asset.fiat.Fiat;
import com.musicslayer.cryptobuddy.crash.CrashView;

public class ReplaceCustomFiatDialog extends BaseDialog {
    public Fiat oldFiat;
    public Fiat newFiat;

    public ReplaceCustomFiatDialog(Activity activity, Fiat oldFiat, Fiat newFiat) {
        super(activity);
        this.oldFiat = oldFiat;
        this.newFiat = newFiat;
    }

    public int getBaseViewID() {
        return R.id.replace_custom_fiat_dialog;
    }

    public void createLayout(Bundle savedInstanceState) {
        setContentView(R.layout.dialog_replace_custom_fiat);

        TextView T = findViewById(R.id.replace_custom_fiat_dialog_textView);
        String text = "A custom fiat with this name already exists. Would you like to replace it?\n\n" +
            "Existing Fiat:\n" +
            "  Name = " + oldFiat.getDisplayName() + "\n" +
            "  Symbol = " + oldFiat.getName() + "\n" +
            "  Decimals = " + oldFiat.getScale() + "\n" +
            "\nNew Fiat:\n" +
            "  Name = " + newFiat.getDisplayName() + "\n" +
            "  Symbol = " + newFiat.getName() + "\n" +
            "  Decimals = " + newFiat.getScale();

        T.setText(text);

        Button cancelButton = findViewById(R.id.replace_custom_fiat_dialog_cancelButton);
        cancelButton.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            @Override
            public void onClickImpl(View view) {
                dismiss();
            }
        });

        Button confirmButton = findViewById(R.id.replace_custom_fiat_dialog_confirmButton);
        confirmButton.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            @Override
            public void onClickImpl(View view) {
                isComplete = true;
                dismiss();
            }
        });
    }
}
