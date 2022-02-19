package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.api.exchange.CryptoExchange;
import com.musicslayer.cryptobuddy.crash.CrashAdapterView;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.util.AuthUtil;
import com.musicslayer.cryptobuddy.util.ToastUtil;
import com.musicslayer.cryptobuddy.view.BorderedSpinnerView;

import java.util.ArrayList;

public class AuthorizeExchangeDialog extends BaseDialog {
    public ArrayList<CryptoExchange> cryptoExchangeArrayList;

    public AuthorizeExchangeDialog(Activity activity, ArrayList<CryptoExchange> cryptoExchangeArrayList) {
        super(activity);
        this.cryptoExchangeArrayList = cryptoExchangeArrayList;
    }

    public int getBaseViewID() {
        return R.id.authorize_exchange_dialog;
    }

    public void createLayout(Bundle savedInstanceState) {
        setContentView(R.layout.dialog_authorize_exchange);

        ArrayList<String> options = new ArrayList<>();
        for(CryptoExchange cryptoExchange : cryptoExchangeArrayList) {
            options.add(cryptoExchange.exchange.toString());
        }

        TextView T = findViewById(R.id.authorize_exchange_dialog_exchangeStatusView);
        Button B_AUTHORIZE_BROWSER = findViewById(R.id.authorize_exchange_dialog_authorizeBrowserButton);

        BorderedSpinnerView bsv = findViewById(R.id.authorize_exchange_dialog_spinner);
        bsv.setOptions(options);
        bsv.setOnItemSelectedListener(new CrashAdapterView.CrashOnItemSelectedListener(this.activity) {
            public void onNothingSelectedImpl(AdapterView<?> parent) {}
            public void onItemSelectedImpl(AdapterView<?> parent, View view, int pos, long id) {
                CryptoExchange cryptoExchange = cryptoExchangeArrayList.get(pos);

                setAuthorizedListeners(activity, cryptoExchange);
                setAuthorizedDisplay(cryptoExchange, cryptoExchange.isAuthorized());

                B_AUTHORIZE_BROWSER.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
                    public void onClickImpl(View v) {
                        authorizeBrowser(cryptoExchange);
                    }
                });
            }
        });

        if(cryptoExchangeArrayList.size() == 1) {
            bsv.setVisibility(View.GONE);
        }

        if(cryptoExchangeArrayList.size() == 0) {
            bsv.setVisibility(View.GONE);
            B_AUTHORIZE_BROWSER.setVisibility(View.GONE);
            T.setText("No exchanges found.");
        }
    }

    public void setAuthorizedDisplay(CryptoExchange cryptoExchange, boolean isAuthorized) {
        TextView T = findViewById(R.id.authorize_exchange_dialog_exchangeStatusView);
        if(isAuthorized) {
            T.setText(cryptoExchange.toString() + " = Authorized");
            T.setTextColor(0xFF000000);
        }
        else {
            T.setText(cryptoExchange.toString() + " = Unauthorized");
            T.setTextColor(0xFFFF0000);
        }
    }

    public void setAuthorizedListeners(Context context, CryptoExchange cryptoExchange) {
        if(cryptoExchange.exchangeAPI != null) {
            cryptoExchange.exchangeAPI.restoreListeners(context, new AuthUtil.AuthorizationListener() {
                @Override
                public void onAuthorization() {
                    setAuthorizedDisplay(cryptoExchange, true);
                }
            });
        }
    }

    public void authorizeBrowser(CryptoExchange cryptoExchange) {
        if(cryptoExchange.exchangeAPI != null) {
            cryptoExchange.exchangeAPI.authorize(activity, new AuthUtil.AuthorizationListener() {
                @Override
                public void onAuthorization() {
                    setAuthorizedDisplay(cryptoExchange, true);
                }
            });
        }
        else {
            ToastUtil.showToast("authorization_failed");
        }
    }
}