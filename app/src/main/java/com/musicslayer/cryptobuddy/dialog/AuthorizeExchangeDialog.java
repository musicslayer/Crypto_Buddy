package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.api.exchange.ExchangeAPI;
import com.musicslayer.cryptobuddy.api.exchange.ExchangeData;
import com.musicslayer.cryptobuddy.asset.exchange.Exchange;
import com.musicslayer.cryptobuddy.crash.CrashAdapterView;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.util.AuthUtil;
import com.musicslayer.cryptobuddy.view.BorderedSpinnerView;

import java.util.ArrayList;

// Dialog that wraps a WebView for the user to interact with a web page to grant an exchange authorization.

public class AuthorizeExchangeDialog extends BaseDialog {
    public ExchangeAPI user_EXCHANGEAPI;

    public ArrayList<Exchange> exchangeArrayList;

    public AuthorizeExchangeDialog(Activity activity, ArrayList<Exchange> exchangeArrayList) {
        super(activity);
        this.exchangeArrayList = exchangeArrayList;
    }

    public int getBaseViewID() {
        return R.id.authorize_exchange_dialog;
    }

    public void createLayout() {
        setContentView(R.layout.dialog_authorize_exchange);

        ArrayList<String> options = new ArrayList<>();
        for(Exchange exchange : exchangeArrayList) {
            options.add(exchange.toString());
        }

        TextView T = findViewById(R.id.authorize_exchange_dialog_textView);
        Button B_AUTHORIZE = findViewById(R.id.authorize_exchange_dialog_authorizeButton);

        BorderedSpinnerView bsv = findViewById(R.id.authorize_exchange_dialog_spinner);
        bsv.setOptions(options);
        bsv.setOnItemSelectedListener(new CrashAdapterView.CrashOnItemSelectedListener(this.activity) {
            public void onNothingSelectedImpl(AdapterView<?> parent) {}
            public void onItemSelectedImpl(AdapterView<?> parent, View view, int pos, long id) {
                Exchange exchange = exchangeArrayList.get(pos);

                T.setText(exchange.toString());

                B_AUTHORIZE.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
                    public void onClickImpl(View v) {
                        ExchangeAPI exchangeAPI = ExchangeData.getExchangeAPI(exchange);
                        exchangeAPI.authorize(activity, new AuthUtil.AuthorizationListener() {
                            @Override
                            public void onAuthorization() {
                                // Only set this if we successfully authorized.
                                user_EXCHANGEAPI = exchangeAPI;
                            }
                        });
                    }
                });
            }
        });

        if(exchangeArrayList.size() == 1) {
            bsv.setVisibility(View.GONE);
        }

        if(exchangeArrayList.size() == 0) {
            bsv.setVisibility(View.GONE);
            B_AUTHORIZE.setVisibility(View.GONE);
            T.setText("No exchanges found.");
        }
    }
}