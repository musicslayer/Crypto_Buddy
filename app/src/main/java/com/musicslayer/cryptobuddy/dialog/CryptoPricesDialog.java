package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.asset.fiat.USD;
import com.musicslayer.cryptobuddy.transaction.AssetQuantity;
import com.musicslayer.cryptobuddy.transaction.AssetPrice;
import com.musicslayer.cryptobuddy.api.price.PriceData;
import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.persistence.Settings;
import com.musicslayer.cryptobuddy.view.SelectAndSearchView;

public class CryptoPricesDialog extends BaseDialog {
    final PriceData[] priceData = new PriceData[1];
    final Crypto[] crypto = new Crypto[1];

    public CryptoPricesDialog(Activity activity) {
        super(activity);
    }

    public int getBaseViewID() {
        return R.id.crypto_prices_dialog;
    }

    public void createLayout() {
        setContentView(R.layout.dialog_crypto_prices);

        TextView T = findViewById(R.id.crypto_prices_dialog_textView);
        SelectAndSearchView ssv = findViewById(R.id.crypto_prices_dialog_selectAndSearchView);
        ssv.setIncludesFiat(this.activity, false);
        ssv.setOptionsCoin();

        ProgressDialogFragment progressDialogFragment = ProgressDialogFragment.newInstance(ProgressDialog.class);
        progressDialogFragment.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                priceData[0] = PriceData.getPriceData(crypto[0]);
            }
        });

        progressDialogFragment.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if(priceData[0].alertUser()) {
                    T.setText("");
                }
                else {
                    AssetPrice assetPrice = priceData[0].getAssetPrice();
                    AssetQuantity marketCapAssetQuantity = new AssetQuantity(priceData[0].usdMarketCap, new USD());

                    String text = "Forward Price = " + assetPrice.toString();
                    if("ForwardBackward".equals(Settings.setting_price)) {
                        text = text + "\nBackward Price = " + assetPrice.reverseAssetPrice().toString();
                    }
                    text = text + "\nMarket Cap = " + marketCapAssetQuantity.toString() + "\nData Source = CoinGecko API V3";

                    T.setText(text);
                }
            }
        });
        progressDialogFragment.restoreListeners(this.activity, "progress");

        Button B = findViewById(R.id.crypto_prices_dialog_button);
        B.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                crypto[0] = (Crypto)ssv.getChosenAsset();
                progressDialogFragment.show(CryptoPricesDialog.this.activity, "progress");
            }
        });
    }

    @Override
    public Bundle onSaveInstanceState() {
        super.onSaveInstanceState();

        Bundle bundle = super.onSaveInstanceState();
        bundle.putSerializable("priceData", priceData[0]);
        bundle.putSerializable("crypto", crypto[0]);
        return bundle;
    }

    @Override
    public void onRestoreInstanceState(Bundle bundle) {
        if(bundle != null) {
            priceData[0] = (PriceData)bundle.getSerializable("priceData");
            crypto[0] = (Crypto)bundle.getSerializable("crypto");
        }

        super.onRestoreInstanceState(bundle);
    }
}
