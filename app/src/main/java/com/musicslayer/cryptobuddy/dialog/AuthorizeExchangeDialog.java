package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
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
import com.musicslayer.cryptobuddy.state.StateObj;
import com.musicslayer.cryptobuddy.util.AuthUtil;
import com.musicslayer.cryptobuddy.util.HashMapUtil;
import com.musicslayer.cryptobuddy.util.ToastUtil;
import com.musicslayer.cryptobuddy.view.BorderedSpinnerView;

import java.util.ArrayList;
import java.util.HashMap;

public class AuthorizeExchangeDialog extends BaseDialog {
    public ArrayList<Exchange> exchangeArrayList;
    public HashMap<Exchange, ExchangeAPI> exchangeAPIMap;

    public AuthorizeExchangeDialog(Activity activity, ArrayList<Exchange> exchangeArrayList) {
        super(activity);
        this.exchangeArrayList = exchangeArrayList;
        this.exchangeAPIMap = StateObj.exchangeAPIMap;
    }

    public int getBaseViewID() {
        return R.id.authorize_exchange_dialog;
    }

    public void createLayout(Bundle savedInstanceState) {
        setContentView(R.layout.dialog_authorize_exchange);

        ArrayList<String> options = new ArrayList<>();
        for(Exchange exchange : exchangeArrayList) {
            options.add(exchange.toString());
        }

        TextView T = findViewById(R.id.authorize_exchange_dialog_exchangeStatusView);
        Button B_AUTHORIZE_BROWSER = findViewById(R.id.authorize_exchange_dialog_authorizeBrowserButton);
        Button B_AUTHORIZE_WEBVIEW = findViewById(R.id.authorize_exchange_dialog_authorizeWebViewButton);

        // TODO For now, WebView is really buggy on recreation, so don't offer this option. But in the future, we may wish to offer this.
        // android_util_AssetManager.cpp:1194] Check failed: theme->GetAssetManager() == &(*assetmanager)
        B_AUTHORIZE_WEBVIEW.setVisibility(View.GONE);

        BorderedSpinnerView bsv = findViewById(R.id.authorize_exchange_dialog_spinner);
        bsv.setOptions(options);
        bsv.setOnItemSelectedListener(new CrashAdapterView.CrashOnItemSelectedListener(this.activity) {
            public void onNothingSelectedImpl(AdapterView<?> parent) {}
            public void onItemSelectedImpl(AdapterView<?> parent, View view, int pos, long id) {
                Exchange exchange = exchangeArrayList.get(pos);
                ExchangeAPI exchangeAPI = ExchangeData.getExchangeAPI(exchange);

                // Each exchange has at most one supported API, so just set it in the map here unconditionally.
                HashMapUtil.putValueInMap(exchangeAPIMap, exchange, exchangeAPI);

                setAuthorizedListeners(activity, exchange, exchangeAPI);
                setAuthorizedDisplay(exchange, exchangeAPI != null && exchangeAPI.isAuthorized());

                B_AUTHORIZE_BROWSER.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
                    public void onClickImpl(View v) {
                        ExchangeAPI exchangeAPI = HashMapUtil.getValueFromMap(exchangeAPIMap, exchange);
                        authorizeBrowser(activity, exchangeAPI);
                    }
                });

                B_AUTHORIZE_WEBVIEW.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
                    public void onClickImpl(View v) {
                        ExchangeAPI exchangeAPI = HashMapUtil.getValueFromMap(exchangeAPIMap, exchange);
                        authorizeWebView(activity, exchangeAPI);
                    }
                });
            }
        });

        if(exchangeArrayList.size() == 1) {
            bsv.setVisibility(View.GONE);
        }

        if(exchangeArrayList.size() == 0) {
            bsv.setVisibility(View.GONE);
            B_AUTHORIZE_BROWSER.setVisibility(View.GONE);
            B_AUTHORIZE_WEBVIEW.setVisibility(View.GONE);
            T.setText("No exchanges found.");
        }
    }

    public void setAuthorizedDisplay(Exchange exchange, boolean isAuthorized) {
        TextView T = findViewById(R.id.authorize_exchange_dialog_exchangeStatusView);
        if(isAuthorized) {
            T.setText(exchange.toString() + " = Authorized");
            T.setTextColor(0xFF00FF00);
        }
        else {
            T.setText(exchange.toString() + " = Unauthorized");
            T.setTextColor(0xFFFF0000);
        }
    }

    public void setAuthorizedListeners(Context context, Exchange exchange, ExchangeAPI exchangeAPI) {
        if(exchangeAPI != null) {
            exchangeAPI.restoreListeners(context, new AuthUtil.AuthorizationListener() {
                @Override
                public void onAuthorization() {
                    setAuthorizedDisplay(exchange, true);
                }
            });
        }
    }

    public void authorizeWebView(Context context, ExchangeAPI exchangeAPI) {
        if(exchangeAPI != null) {
            exchangeAPI.authorizeWebView(activity);
        }
        else {
            ToastUtil.showToast(context, "authorization_failed");
        }
    }

    public void authorizeBrowser(Context context, ExchangeAPI exchangeAPI) {
        if(exchangeAPI != null) {
            exchangeAPI.authorizeBrowser(activity);
        }
        else {
            ToastUtil.showToast(context, "authorization_failed");
        }
    }
}