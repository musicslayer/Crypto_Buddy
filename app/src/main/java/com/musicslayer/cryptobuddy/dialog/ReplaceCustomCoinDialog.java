package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin_Impl;
import com.musicslayer.cryptobuddy.crash.CrashView;

public class ReplaceCustomCoinDialog extends BaseDialog {
    public Coin_Impl oldCoin;
    public Coin_Impl newCoin;

    public ReplaceCustomCoinDialog(Activity activity, Coin_Impl oldCoin, Coin_Impl newCoin) {
        super(activity);
        this.oldCoin = oldCoin;
        this.newCoin = newCoin;
    }

    public int getBaseViewID() {
        return R.id.replace_custom_coin_dialog;
    }

    public void createLayout(Bundle savedInstanceState) {
        setContentView(R.layout.dialog_replace_custom_coin);

        TextView T = findViewById(R.id.replace_custom_coin_dialog_textView);
        String text = "A custom coin with this name already exists. Would you like to replace it?\n\n" +
            "Existing Coin:\n" +
            "  Name = " + oldCoin.getDisplayName() + "\n" +
            "  Symbol = " + oldCoin.getName() + "\n" +
            "  Decimals = " + oldCoin.getScale() + "\n" +
            "\nNew Coin:\n" +
            "  Name = " + newCoin.getDisplayName() + "\n" +
            "  Symbol = " + newCoin.getName() + "\n" +
            "  Decimals = " + newCoin.getScale();

        T.setText(text);

        Button cancelButton = findViewById(R.id.replace_custom_coin_dialog_cancelButton);
        cancelButton.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            @Override
            public void onClickImpl(View view) {
                dismiss();
            }
        });

        Button confirmButton = findViewById(R.id.replace_custom_coin_dialog_confirmButton);
        confirmButton.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            @Override
            public void onClickImpl(View view) {
                isComplete = true;
                dismiss();
            }
        });
    }
}
