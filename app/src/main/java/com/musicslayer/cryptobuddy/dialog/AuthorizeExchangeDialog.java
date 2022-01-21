package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
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

        TextView T = findViewById(R.id.authorize_exchange_dialog_textView);
        Button B_AUTHORIZE = findViewById(R.id.authorize_exchange_dialog_authorizeButton);
        //TextView TA = findViewById(R.id.authorize_exchange_dialog_authorizeTextView);

        BorderedSpinnerView bsv = findViewById(R.id.authorize_exchange_dialog_spinner);
        bsv.setOptions(options);
        bsv.setOnItemSelectedListener(new CrashAdapterView.CrashOnItemSelectedListener(this.activity) {
            public void onNothingSelectedImpl(AdapterView<?> parent) {}
            public void onItemSelectedImpl(AdapterView<?> parent, View view, int pos, long id) {
                Exchange exchange = exchangeArrayList.get(pos);
                ExchangeAPI exchangeAPI = HashMapUtil.getValueFromMap(exchangeAPIMap, exchange);

                if(exchangeAPI != null) {
                    exchangeAPI.restoreListeners(activity, new AuthUtil.AuthorizationListener() {
                        @Override
                        public void onAuthorization() {
                            //TA.setText("Authorized");
                            //TA.setTextColor(0xFF00FF00);

                            T.setText(exchange.toString() + " = Authorized");
                            T.setTextColor(0xFF00FF00);
                        }
                    });
                }

                //T.setText(exchange.toString());

                if(exchangeAPI != null && exchangeAPI.isAuthorized()) {
                    //TA.setText("Authorized");
                    //TA.setTextColor(0xFF00FF00);

                    T.setText(exchange.toString() + " = Authorized");
                    T.setTextColor(0xFF00FF00);
                }
                else {
                    //TA.setText("Unauthorized");
                    //TA.setTextColor(0xFFFF0000);

                    T.setText(exchange.toString() + " = Unauthorized");
                    T.setTextColor(0xFFFF0000);
                }

                B_AUTHORIZE.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
                    public void onClickImpl(View v) {
                        // Look for a supported ExchangeAPI for this exchange and then try to authenticate it.
                        ExchangeAPI newExchangeAPI = ExchangeData.getExchangeAPI(exchange);
                        HashMapUtil.putValueInMap(exchangeAPIMap, exchange, newExchangeAPI);

                        newExchangeAPI.restoreListeners(activity, new AuthUtil.AuthorizationListener() {
                            @Override
                            public void onAuthorization() {
                                //TA.setText("Authorized");
                                //TA.setTextColor(0xFF00FF00);

                                T.setText(exchange.toString() + " = Authorized");
                                T.setTextColor(0xFF00FF00);
                            }
                        });

                        newExchangeAPI.authorize(activity);
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
            //TA.setVisibility(View.GONE);
            T.setText("No exchanges found.");
        }
    }
}