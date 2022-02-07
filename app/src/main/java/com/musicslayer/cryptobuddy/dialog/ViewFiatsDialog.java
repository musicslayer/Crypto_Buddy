package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.os.Bundle;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.asset.fiat.Fiat;
import com.musicslayer.cryptobuddy.asset.fiatmanager.FiatManager;
import com.musicslayer.cryptobuddy.view.SelectAndSearchView;

import java.util.ArrayList;

public class ViewFiatsDialog extends BaseDialog {
    public ViewFiatsDialog(Activity activity) {
        super(activity);
    }

    public int getBaseViewID() {
        return R.id.view_fiats_dialog;
    }

    public void createLayout(Bundle savedInstanceState) {
        setContentView(R.layout.dialog_view_fiats);

        SelectAndSearchView ssv = findViewById(R.id.view_fiats_dialog_selectAndSearchView);
        ssv.setIncludesFiat(true);
        ssv.setIncludesCoin(false);
        ssv.setIncludesToken(false);

        ArrayList<Fiat> fiatArrayList = new ArrayList<>(FiatManager.getDefaultFiatManager().getFiats());
        ssv.setFiatOptions(fiatArrayList);

        ssv.chooseFiat();

        updateLayout();
    }

    public void updateLayout() {
    }
}
