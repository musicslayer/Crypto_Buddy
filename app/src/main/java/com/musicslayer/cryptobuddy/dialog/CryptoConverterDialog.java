package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.persistence.Settings;
import com.musicslayer.cryptobuddy.transaction.AssetQuantity;
import com.musicslayer.cryptobuddy.transaction.AssetPrice;
import com.musicslayer.cryptobuddy.api.price.PriceData;
import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.util.Toast;
import com.musicslayer.cryptobuddy.view.red.NumericEditText;
import com.musicslayer.cryptobuddy.view.SelectAndSearchView;

public class CryptoConverterDialog extends BaseDialog {
    final PriceData[] priceData = new PriceData[2];
    final Crypto[] cryptoPrimary = new Crypto[1];
    final Crypto[] cryptoSecondary = new Crypto[1];

    public CryptoConverterDialog(Activity activity) {
        super(activity);
    }

    public int getBaseViewID() {
        return R.id.crypto_converter_dialog;
    }

    public void createLayout() {
        setContentView(R.layout.dialog_crypto_converter);

        TextView T = findViewById(R.id.crypto_converter_dialog_textView);

        NumericEditText E_PRIMARYASSET = findViewById(R.id.crypto_converter_dialog_primaryEditText);

        SelectAndSearchView ssvPrimary = findViewById(R.id.crypto_converter_dialog_primarySelectAndSearchView);
        ssvPrimary.setIncludesFiat(this.activity, false);
        ssvPrimary.setOptionsCoin();

        SelectAndSearchView ssvSecondary = findViewById(R.id.crypto_converter_dialog_secondarySelectAndSearchView);
        ssvSecondary.setIncludesFiat(this.activity, false);
        ssvSecondary.setOptionsCoin();

        ProgressDialogFragment progressDialogFragment = ProgressDialogFragment.newInstance(ProgressDialog.class);
        progressDialogFragment.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                priceData[0] = PriceData.getPriceData(cryptoPrimary[0]);
                if(((ProgressDialog)dialog).isCancelled) { return; }
                priceData[1] = PriceData.getPriceData(cryptoSecondary[0]);
            }
        });

        progressDialogFragment.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                // We need two separate branches so the alert Toast shows correctly.
                if(priceData[0].alertUser()) {
                    T.setText("");
                }
                else if(priceData[1].alertUser()) {
                    T.setText("");
                }
                else {
                    AssetQuantity primaryAssetQuantity = new AssetQuantity(E_PRIMARYASSET.getText().toString(), cryptoPrimary[0]);
                    AssetPrice primaryAssetPrice = priceData[0].getAssetPrice();
                    AssetPrice secondaryAssetPrice = priceData[1].getAssetPrice();
                    AssetQuantity secondaryAssetQuantity = primaryAssetQuantity.convert(primaryAssetPrice).convert(secondaryAssetPrice.reverseAssetPrice());

                    String text = "Conversion:\n" + primaryAssetQuantity.toString() + " = " + secondaryAssetQuantity.toString() +
                            "\n\nForward Prices:\n" + primaryAssetPrice.toString() +
                            "\n" + secondaryAssetPrice.toString();

                    if("ForwardBackward".equals(Settings.setting_price)) {
                        text = text + "\n\nBackward Prices:\n" + primaryAssetPrice.reverseAssetPrice().toString() +
                                "\n" + secondaryAssetPrice.reverseAssetPrice().toString();
                    }

                    text = text + "\n\nData Source = CoinGecko API V3";

                    T.setText(text);
                }
            }
        });
        progressDialogFragment.restoreListeners(this.activity, "progress");

        Button B_CONVERT = findViewById(R.id.crypto_converter_dialog_convertButton);
        B_CONVERT.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                cryptoPrimary[0] = (Crypto)ssvPrimary.getChosenAsset();
                cryptoSecondary[0] = (Crypto)ssvSecondary.getChosenAsset();

                boolean isValid = E_PRIMARYASSET.test();

                if(cryptoPrimary[0] == cryptoSecondary[0]) {
                    Toast.showToast("cryptos_same");
                    return;
                }

                if(isValid) {
                    progressDialogFragment.show(CryptoConverterDialog.this.activity, "progress");
                }
            }
        });

        FloatingActionButton fab_swap = findViewById(R.id.crypto_converter_dialog_swapButton);
        fab_swap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SelectAndSearchView.swap(ssvPrimary, ssvSecondary);
            }
        });
    }

    @Override
    public Bundle onSaveInstanceState() {
        super.onSaveInstanceState();

        Bundle bundle = super.onSaveInstanceState();
        bundle.putSerializable("priceData", priceData[0]);
        bundle.putSerializable("cryptoPrimary", cryptoPrimary[0]);
        bundle.putSerializable("cryptoSecondary", cryptoSecondary[0]);
        return bundle;
    }

    @Override
    public void onRestoreInstanceState(Bundle bundle) {
        if(bundle != null) {
            priceData[0] = (PriceData)bundle.getSerializable("priceData");
            cryptoPrimary[0] = (Crypto)bundle.getSerializable("cryptoPrimary");
            cryptoSecondary[0] = (Crypto)bundle.getSerializable("cryptoSecondary");
        }

        super.onRestoreInstanceState(bundle);
    }
}
