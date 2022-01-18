package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.activity.ExchangeExplorerActivity;
import com.musicslayer.cryptobuddy.activity.ExchangePortfolioExplorerActivity;
import com.musicslayer.cryptobuddy.api.exchange.ExchangeData;
import com.musicslayer.cryptobuddy.asset.exchange.Exchange;
import com.musicslayer.cryptobuddy.crash.CrashAdapterView;
import com.musicslayer.cryptobuddy.util.HashMapUtil;
import com.musicslayer.cryptobuddy.view.BorderedSpinnerView;

import java.util.ArrayList;
import java.util.HashMap;

public class ExchangeInfoDialog extends BaseDialog {
    ArrayList<Exchange> exchangeArrayList;
    HashMap<Exchange, ExchangeData> exchangeDataMap;

    public ExchangeInfoDialog(Activity activity, ArrayList<Exchange> exchangeArrayList) {
        super(activity);
        this.exchangeArrayList = exchangeArrayList;

        if(activity instanceof ExchangeExplorerActivity) {
            this.exchangeDataMap = ((ExchangeExplorerActivity)activity).activityStateObj[0].exchangeDataMap;
        }
        else if(activity instanceof ExchangePortfolioExplorerActivity) {
            this.exchangeDataMap = ((ExchangePortfolioExplorerActivity)activity).activityStateObj[0].exchangeDataMap;
        }
    }

    public int getBaseViewID() {
        return R.id.exchange_info_dialog;
    }

    public void createLayout(Bundle savedInstanceState) {
        setContentView(R.layout.dialog_exchange_info);

        TextView T = findViewById(R.id.exchange_info_dialog_textView);

        ArrayList<String> options = new ArrayList<>();
        for(Exchange exchange : exchangeArrayList) {
            options.add(exchange.toString());
        }

        BorderedSpinnerView bsv = findViewById(R.id.exchange_info_dialog_spinner);
        bsv.setOptions(options);
        bsv.setOnItemSelectedListener(new CrashAdapterView.CrashOnItemSelectedListener(this.activity) {
            public void onNothingSelectedImpl(AdapterView<?> parent) {}
            public void onItemSelectedImpl(AdapterView<?> parent, View view, int pos, long id) {
                Exchange exchange = exchangeArrayList.get(pos);
                ExchangeData exchangeData = HashMapUtil.getValueFromMap(exchangeDataMap, exchange);
                T.setText(exchangeData.getInfoString());
            }
        });

        if(exchangeArrayList.size() == 1) {
            bsv.setVisibility(View.GONE);
        }

        if(exchangeArrayList.size() == 0) {
            bsv.setVisibility(View.GONE);
            T.setText("No exchanges found.");
        }
    }
}