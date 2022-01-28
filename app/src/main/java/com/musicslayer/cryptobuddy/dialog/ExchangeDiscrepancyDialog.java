package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.api.exchange.ExchangeData;
import com.musicslayer.cryptobuddy.asset.exchange.Exchange;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.state.StateObj;
import com.musicslayer.cryptobuddy.util.HelpUtil;

import java.util.ArrayList;
import java.util.HashMap;

public class ExchangeDiscrepancyDialog extends BaseDialog {
    ArrayList<Exchange> exchangeAddressArrayList;
    HashMap<Exchange, ExchangeData> exchangeDataMap;

    public ExchangeDiscrepancyDialog(Activity activity, ArrayList<Exchange> exchangeAddressArrayList) {
        super(activity);
        this.exchangeAddressArrayList = exchangeAddressArrayList;
        this.exchangeDataMap = StateObj.exchangeDataMap;
    }

    public int getBaseViewID() {
        return R.id.exchange_discrepancy_dialog;
    }

    public void createLayout(Bundle savedInstanceState) {
        setContentView(R.layout.dialog_exchange_discrepancy);

        ImageButton helpButton = findViewById(R.id.exchange_discrepancy_dialog_helpButton);
        helpButton.setOnClickListener(new CrashView.CrashOnClickListener(activity) {
            @Override
            public void onClickImpl(View view) {
                HelpUtil.showHelp(activity, R.raw.help_discrepancy);
            }
        });

        // TODO Implement this...
        TextView T = findViewById(R.id.exchange_discrepancy_dialog_assetTextView);
        T.setText("\nThis exchange has no discrepancies.");
    }
}