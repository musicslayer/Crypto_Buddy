package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.asset.fiat.Fiat;
import com.musicslayer.cryptobuddy.crash.CrashDialogInterface;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.settings.setting.PriceDisplaySetting;
import com.musicslayer.cryptobuddy.transaction.AssetQuantity;
import com.musicslayer.cryptobuddy.transaction.AssetPrice;
import com.musicslayer.cryptobuddy.api.price.PriceData;
import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.serialize.Serialization;
import com.musicslayer.cryptobuddy.util.HelpUtil;
import com.musicslayer.cryptobuddy.util.ToastUtil;
import com.musicslayer.cryptobuddy.view.red.NumericEditText;
import com.musicslayer.cryptobuddy.view.SelectAndSearchView;

import java.util.ArrayList;

public class CryptoConverterDialog extends BaseDialog {
    Crypto cryptoPrimary;
    Crypto cryptoSecondary;

    public CryptoConverterDialog(Activity activity) {
        super(activity);
    }

    public int getBaseViewID() {
        return R.id.crypto_converter_dialog;
    }

    public void createLayout(Bundle savedInstanceState) {
        setContentView(R.layout.dialog_crypto_converter);

        ImageButton helpButton = findViewById(R.id.crypto_converter_dialog_helpButton);
        helpButton.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            @Override
            public void onClickImpl(View view) {
                HelpUtil.showHelp(activity, R.raw.help_crypto_converter);
            }
        });

        TextView T = findViewById(R.id.crypto_converter_dialog_textView);

        NumericEditText E_PRIMARYASSET = findViewById(R.id.crypto_converter_dialog_primaryEditText);

        SelectAndSearchView ssvPrimary = findViewById(R.id.crypto_converter_dialog_primarySelectAndSearchView);
        ssvPrimary.setIncludesFiat(false);
        ssvPrimary.setIncludesCoin(true);
        ssvPrimary.setIncludesToken(true);
        ssvPrimary.setOptionsCoin();

        SelectAndSearchView ssvSecondary = findViewById(R.id.crypto_converter_dialog_secondarySelectAndSearchView);
        ssvSecondary.setIncludesFiat(false);
        ssvSecondary.setIncludesCoin(true);
        ssvSecondary.setIncludesToken(true);
        ssvSecondary.setOptionsCoin();

        SelectAndSearchView fssv = findViewById(R.id.crypto_converter_dialog_fiatSelectAndSearchView);
        fssv.setIncludesFiat(true);
        fssv.setIncludesCoin(false);
        fssv.setIncludesToken(false);
        fssv.setOptionsFiat();

        ProgressDialogFragment progressDialogFragment = ProgressDialogFragment.newInstance(ProgressDialog.class);
        progressDialogFragment.setOnShowListener(new CrashDialogInterface.CrashOnShowListener(this.activity) {
            @Override
            public void onShowImpl(DialogInterface dialog) {
                ProgressDialogFragment.updateProgressTitle("Performing Conversion...");

                Fiat priceFiat = (Fiat)fssv.getChosenAsset();

                PriceData priceDataPrimary = PriceData.getPriceData(cryptoPrimary, priceFiat);
                if(ProgressDialogFragment.isCancelled()) { return; }
                PriceData priceDataSecondary = PriceData.getPriceData(cryptoSecondary, priceFiat);

                ArrayList<PriceData> priceDataArrayList = new ArrayList<>();
                priceDataArrayList.add(priceDataPrimary);
                priceDataArrayList.add(priceDataSecondary);

                ProgressDialogFragment.setValue(Serialization.serializeArrayList(priceDataArrayList));
            }
        });

        progressDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this.activity) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                ArrayList<PriceData> priceDataArrayList = Serialization.deserializeArrayList(ProgressDialogFragment.getValue(), PriceData.class);
                PriceData priceDataPrimary = priceDataArrayList.get(0);
                PriceData priceDataSecondary = priceDataArrayList.get(1);

                if(priceDataPrimary.isPriceComplete() && priceDataSecondary.isPriceComplete()) {
                    AssetQuantity primaryAssetQuantity = new AssetQuantity(E_PRIMARYASSET.getTextString(), cryptoPrimary);
                    AssetPrice primaryAssetPrice = new AssetPrice(new AssetQuantity("1", priceDataPrimary.crypto), priceDataPrimary.price);
                    AssetPrice secondaryAssetPrice = new AssetPrice(new AssetQuantity("1", priceDataSecondary.crypto), priceDataSecondary.price);
                    AssetQuantity secondaryAssetQuantity = primaryAssetQuantity.convert(primaryAssetPrice).convert(secondaryAssetPrice.reverseAssetPrice());

                    String text = "Conversion:\n" + primaryAssetQuantity + " = " + secondaryAssetQuantity.toString() +
                            "\n\nForward Prices:\n" + primaryAssetPrice +
                            "\n" + secondaryAssetPrice;

                    if("ForwardBackward".equals(PriceDisplaySetting.value)) {
                        text = text + "\n\nBackward Prices:\n" + primaryAssetPrice.reverseAssetPrice().toString() +
                                "\n" + secondaryAssetPrice.reverseAssetPrice().toString();
                    }

                    text = text + "\n\nData Source = CoinGecko API V3";

                    T.setText(text);
                }
                else {
                    T.setText("");
                    ToastUtil.showToast(activity,"incomplete_price_data");
                }
            }
        });
        progressDialogFragment.restoreListeners(this.activity, "progress");

        Button B_CONVERT = findViewById(R.id.crypto_converter_dialog_convertButton);
        B_CONVERT.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                cryptoPrimary = (Crypto)ssvPrimary.getChosenAsset();
                cryptoSecondary = (Crypto)ssvSecondary.getChosenAsset();

                if(E_PRIMARYASSET.test()) {
                    progressDialogFragment.show(CryptoConverterDialog.this.activity, "progress");
                }
            }
        });

        FloatingActionButton fab_swap = findViewById(R.id.crypto_converter_dialog_swapButton);
        fab_swap.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            @Override
            public void onClickImpl(View view) {
                SelectAndSearchView.swap(ssvPrimary, ssvSecondary);
            }
        });
    }

    @Override
    public Bundle onSaveInstanceStateImpl(Bundle bundle) {
        bundle.putParcelable("cryptoPrimary", cryptoPrimary);
        bundle.putParcelable("cryptoSecondary", cryptoSecondary);
        return bundle;
    }

    @Override
    public void onRestoreInstanceStateImpl(Bundle bundle) {
        if(bundle != null) {
            cryptoPrimary = bundle.getParcelable("cryptoPrimary");
            cryptoSecondary = bundle.getParcelable("cryptoSecondary");
        }
    }
}
