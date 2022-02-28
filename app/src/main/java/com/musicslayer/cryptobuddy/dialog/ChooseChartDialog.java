package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.api.chart.CryptoChart;
import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.asset.fiat.Fiat;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.util.ToastUtil;
import com.musicslayer.cryptobuddy.view.asset.SelectAndSearchView;

public class ChooseChartDialog extends BaseDialog {
    public CryptoChart user_CRYPTOCHART;

    public ChooseChartDialog(Activity activity) {
        super(activity);
    }

    public int getBaseViewID() {
        return R.id.choose_chart_dialog;
    }

    public void createLayout(Bundle savedInstanceState) {
        setContentView(R.layout.dialog_choose_chart);

        SelectAndSearchView ssv = findViewById(R.id.choose_chart_dialog_selectAndSearchView);
        ssv.setIncludesFiat(false);
        ssv.setIncludesCoin(true);
        ssv.setIncludesToken(true);
        ssv.setCompleteOptions();
        ssv.chooseCoin("BASE");

        SelectAndSearchView fssv = findViewById(R.id.choose_chart_dialog_fiatSelectAndSearchView);
        fssv.setIncludesFiat(true);
        fssv.setIncludesCoin(false);
        fssv.setIncludesToken(false);
        fssv.setCompleteOptions();
        fssv.chooseFiat("BASE");

        Button B = findViewById(R.id.choose_chart_dialog_confirmButton);
        B.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                Crypto crypto = (Crypto)ssv.getChosenAsset();
                Fiat priceFiat = (Fiat)fssv.getChosenAsset();

                if(crypto == null || priceFiat == null) {
                    ToastUtil.showToast("must_choose_assets");
                }
                else {
                    user_CRYPTOCHART = new CryptoChart(crypto, priceFiat);
                    isComplete = true;
                    dismiss();
                }
            }
        });
    }
}
