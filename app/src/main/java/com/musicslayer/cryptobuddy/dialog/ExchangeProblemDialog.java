package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.api.exchange.ExchangeData;
import com.musicslayer.cryptobuddy.asset.exchange.Exchange;
import com.musicslayer.cryptobuddy.state.StateObj;

import java.util.ArrayList;
import java.util.HashMap;

public class ExchangeProblemDialog extends BaseDialog {
    ArrayList<Exchange> exchangeArrayList;
    HashMap<Exchange, ExchangeData> exchangeDataMap;

    public ExchangeProblemDialog(Activity activity, ArrayList<Exchange> exchangeArrayList) {
        super(activity);
        this.exchangeArrayList = exchangeArrayList;
        this.exchangeDataMap = StateObj.exchangeDataMap;
    }

    public int getBaseViewID() {
        return R.id.exchange_problem_dialog;
    }

    public void createLayout(Bundle savedInstanceState) {
        setContentView(R.layout.dialog_exchange_problem);

        // To keep things simple, do not separate problem info by exchange. Just combine it all into one piece of text.
        // Also, right now problem info is only based on the exchange, but in the future the exchange data may be used.
        StringBuilder infoText = new StringBuilder();
        ArrayList<String> seenNames = new ArrayList<>();
        boolean isFirst = true;

        for(Exchange exchange : exchangeArrayList) {
            if(seenNames.contains(exchange.getName())) { continue; }

            ExchangeData exchangeData = exchangeDataMap.get(exchange);

            String info = exchangeData.getProblem();
            if(info != null) {
                // For first info, do not put new lines.
                if(!isFirst) {
                    infoText.append("\n\n");
                }

                isFirst = false;
                infoText.append(info);
            }

            seenNames.add(exchange.getName());
        }

        TextView T = findViewById(R.id.exchange_problem_dialog_textView);
        T.setText(infoText.toString());
    }
}