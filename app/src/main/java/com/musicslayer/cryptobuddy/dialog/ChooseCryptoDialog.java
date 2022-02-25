package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.api.chart.CryptoChart;
import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.util.ToastUtil;
import com.musicslayer.cryptobuddy.view.asset.SelectAndSearchView;

public class ChooseCryptoDialog extends BaseDialog {
    public CryptoChart user_CRYPTOCHART;

    public ChooseCryptoDialog(Activity activity) {
        super(activity);
    }

    public int getBaseViewID() {
        return R.id.choose_crypto_dialog;
    }

    public void createLayout(Bundle savedInstanceState) {
        setContentView(R.layout.dialog_choose_crypto);

        SelectAndSearchView ssv = findViewById(R.id.choose_crypto_dialog_selectAndSearchView);
        ssv.setIncludesFiat(false);
        ssv.setIncludesCoin(true);
        ssv.setIncludesToken(true);
        ssv.setCompleteOptions();
        ssv.chooseCoin("BASE");

        Button B = findViewById(R.id.choose_crypto_dialog_confirmButton);
        B.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                Crypto crypto = (Crypto)ssv.getChosenAsset();

                if(crypto == null) {
                    ToastUtil.showToast("must_choose_crypto");
                }
                else {
                    user_CRYPTOCHART = new CryptoChart(crypto);
                    isComplete = true;
                    dismiss();
                }
            }
        });
    }
}
