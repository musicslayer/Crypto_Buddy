package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;

import androidx.appcompat.widget.AppCompatButton;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.api.exchange.CryptoExchange;
import com.musicslayer.cryptobuddy.asset.exchange.Exchange;
import com.musicslayer.cryptobuddy.crash.CrashView;

import java.util.ArrayList;

public class ChooseExchangeDialog extends BaseDialog {
    public CryptoExchange user_CRYPTOEXCHANGE;

    public String code;

    public ChooseExchangeDialog(Activity activity) {
        super(activity);
    }

    public int getBaseViewID() {
        return R.id.choose_exchange_dialog;
    }

    public void createLayout(Bundle savedInstanceState) {
        setContentView(R.layout.dialog_choose_exchange);

        TableLayout table = findViewById(R.id.choose_exchange_dialog_tableLayout);
        table.removeAllViews();

        for(Exchange exchange : Exchange.exchanges) {
            TableRow TR = new TableRow(activity);

            AppCompatButton B = new AppCompatButton(activity);
            B.setText(exchange.getDisplayName());
            B.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_account_balance_24, 0, 0, 0);
            B.setOnClickListener(new CrashView.CrashOnClickListener(activity) {
                @Override
                public void onClickImpl(View view) {
                    // For now, there is exactly one exchangeAPI to go with the exchange.
                    // But in the future, we could have the user choose and copy the logic for choosing an address.
                    ArrayList<CryptoExchange> cryptoExchangeArrayList = CryptoExchange.getAllValidCryptoExchange(exchange);

                    user_CRYPTOEXCHANGE = cryptoExchangeArrayList.get(0);
                    isComplete = true;
                    dismiss();
                }
            });

            TR.addView(B);
            table.addView(TR);
        }
    }
}
