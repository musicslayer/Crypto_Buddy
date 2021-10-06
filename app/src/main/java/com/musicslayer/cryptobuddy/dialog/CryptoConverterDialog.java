package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.crash.CrashOnClickListener;
import com.musicslayer.cryptobuddy.crash.CrashOnDismissListener;
import com.musicslayer.cryptobuddy.crash.CrashOnShowListener;
import com.musicslayer.cryptobuddy.persistence.Settings;
import com.musicslayer.cryptobuddy.transaction.AssetQuantity;
import com.musicslayer.cryptobuddy.transaction.AssetPrice;
import com.musicslayer.cryptobuddy.api.price.PriceData;
import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.util.Serialization;
import com.musicslayer.cryptobuddy.util.Toast;
import com.musicslayer.cryptobuddy.view.red.NumericEditText;
import com.musicslayer.cryptobuddy.view.SelectAndSearchView;

public class CryptoConverterDialog extends BaseDialog {
    PriceData priceDataPrimary;
    PriceData priceDataSecondary;
    Crypto cryptoPrimary;
    Crypto cryptoSecondary;

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
        ssvPrimary.setIncludesFiat(false);
        ssvPrimary.setOptionsCoin();

        SelectAndSearchView ssvSecondary = findViewById(R.id.crypto_converter_dialog_secondarySelectAndSearchView);
        ssvSecondary.setIncludesFiat(false);
        ssvSecondary.setOptionsCoin();

        ProgressDialogFragment progressDialogFragment = ProgressDialogFragment.newInstance(ProgressDialog.class);
        progressDialogFragment.setOnShowListener(new CrashOnShowListener(this.activity) {
            @Override
            public void onShowImpl(DialogInterface dialog) {
                priceDataPrimary = PriceData.getPriceData(cryptoPrimary);
                if(((ProgressDialog)dialog).isCancelled) { return; }
                priceDataSecondary = PriceData.getPriceData(cryptoSecondary);
            }
        });

        progressDialogFragment.setOnDismissListener(new CrashOnDismissListener(this.activity) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(priceDataPrimary.isComplete() && priceDataSecondary.isComplete()) {
                    AssetQuantity primaryAssetQuantity = new AssetQuantity(E_PRIMARYASSET.getText().toString(), cryptoPrimary);
                    AssetPrice primaryAssetPrice = new AssetPrice(new AssetQuantity("1", priceDataPrimary.crypto), priceDataPrimary.price);
                    AssetPrice secondaryAssetPrice = new AssetPrice(new AssetQuantity("1", priceDataSecondary.crypto), priceDataSecondary.price);
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
                else {
                    T.setText("");
                    Toast.showToast(activity,"no_price_data");
                }
            }
        });
        progressDialogFragment.restoreListeners(this.activity, "progress");

        Button B_CONVERT = findViewById(R.id.crypto_converter_dialog_convertButton);
        B_CONVERT.setOnClickListener(new CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                cryptoPrimary = (Crypto)ssvPrimary.getChosenAsset();
                cryptoSecondary = (Crypto)ssvSecondary.getChosenAsset();

                boolean isValid = E_PRIMARYASSET.test();

                if(cryptoPrimary == cryptoSecondary) {
                    Toast.showToast(activity,"cryptos_same");
                    return;
                }

                if(isValid) {
                    progressDialogFragment.show(CryptoConverterDialog.this.activity, "progress");
                }
            }
        });

        FloatingActionButton fab_swap = findViewById(R.id.crypto_converter_dialog_swapButton);
        fab_swap.setOnClickListener(new CrashOnClickListener(this.activity) {
            @Override
            public void onClickImpl(View view) {
                SelectAndSearchView.swap(ssvPrimary, ssvSecondary);
            }
        });
    }

    @Override
    public Bundle onSaveInstanceStateImpl(Bundle bundle) {
        String priceDataPrimary_s = priceDataPrimary == null ? "{}" : Serialization.serialize(priceDataPrimary);
        bundle.putString("priceDataPrimary", priceDataPrimary_s);

        String priceDataSecondary_s = priceDataSecondary == null ? "{}" : Serialization.serialize(priceDataSecondary);
        bundle.putString("priceDataSecondary", priceDataSecondary_s);

        String cryptoPrimary_s = cryptoPrimary == null ? "{}" : Serialization.serialize(cryptoPrimary);
        bundle.putString("cryptoPrimary", cryptoPrimary_s);

        String cryptoSecondary_s = cryptoSecondary == null ? "{}" : Serialization.serialize(cryptoSecondary);
        bundle.putString("cryptoSecondary", cryptoSecondary_s);

        return bundle;
    }

    @Override
    public void onRestoreInstanceStateImpl(Bundle bundle) {
        if(bundle != null) {
            String priceDataPrimary_s = bundle.getString("priceDataPrimary");
            priceDataPrimary = "{}".equals(priceDataPrimary_s) ? null : Serialization.deserialize(priceDataPrimary_s, PriceData.class);

            String priceDataSecondary_s = bundle.getString("priceDataSecondary");
            priceDataSecondary = "{}".equals(priceDataSecondary_s) ? null : Serialization.deserialize(priceDataSecondary_s, PriceData.class);

            String cryptoPrimary_s = bundle.getString("cryptoPrimary");
            cryptoPrimary = "{}".equals(cryptoPrimary_s) ? null : Serialization.deserialize(cryptoPrimary_s, Crypto.class);

            String cryptoSecondary_s = bundle.getString("cryptoSecondary");
            cryptoSecondary = "{}".equals(cryptoSecondary_s) ? null : Serialization.deserialize(cryptoSecondary_s, Crypto.class);
        }
    }
}
