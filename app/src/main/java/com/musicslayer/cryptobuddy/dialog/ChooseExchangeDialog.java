package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.asset.exchange.Coinbase;
import com.musicslayer.cryptobuddy.asset.exchange.Exchange;
import com.musicslayer.cryptobuddy.asset.exchange.STEX;
import com.musicslayer.cryptobuddy.crash.CrashView;

public class ChooseExchangeDialog extends BaseDialog {
    public Exchange user_EXCHANGE;

    public String code;

    public ChooseExchangeDialog(Activity activity) {
        super(activity);
    }

    public int getBaseViewID() {
        return R.id.choose_exchange_dialog;
    }

    public void createLayout(Bundle savedInstanceState) {
        setContentView(R.layout.dialog_choose_exchange);

/*
        Button B_BINANCE = findViewById(R.id.choose_exchange_dialog_binanceButton);
        B_BINANCE.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                user_EXCHANGE = new Binance();
                isComplete = true;
                dismiss();
            }
        });

        Button B_BINANCEUS = findViewById(R.id.choose_exchange_dialog_binanceUSButton);
        B_BINANCEUS.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                user_EXCHANGE = new BinanceUS();
                isComplete = true;
                dismiss();
            }
        });

 */

        Button B_COINBASE = findViewById(R.id.choose_exchange_dialog_coinbaseButton);
        B_COINBASE.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                user_EXCHANGE = new Coinbase();
                isComplete = true;
                dismiss();
            }
        });

        Button B_STEX = findViewById(R.id.choose_exchange_dialog_stexButton);
        B_STEX.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                user_EXCHANGE = new STEX();
                isComplete = true;
                dismiss();
            }
        });
    }
}
