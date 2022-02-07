package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.os.Bundle;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.asset.coinmanager.CoinManager;
import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;
import com.musicslayer.cryptobuddy.view.SelectAndSearchView;

import java.util.ArrayList;

public class ViewCoinsDialog extends BaseDialog {
    public ViewCoinsDialog(Activity activity) {
        super(activity);
    }

    public int getBaseViewID() {
        return R.id.view_coins_dialog;
    }

    public void createLayout(Bundle savedInstanceState) {
        setContentView(R.layout.dialog_view_coins);

        SelectAndSearchView ssv = findViewById(R.id.view_coins_dialog_selectAndSearchView);
        ssv.setIncludesFiat(false);
        ssv.setIncludesCoin(true);
        ssv.setIncludesToken(false);

        ArrayList<Coin> coinArrayList = new ArrayList<>(CoinManager.getDefaultCoinManager().getCoins());
        ssv.setCoinOptions(coinArrayList);

        ssv.chooseCoin();

        updateLayout();
    }

    public void updateLayout() {
    }
}
