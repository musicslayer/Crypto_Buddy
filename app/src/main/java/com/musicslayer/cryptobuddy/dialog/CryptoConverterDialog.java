package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.musicslayer.cryptobuddy.api.price.CryptoPrice;
import com.musicslayer.cryptobuddy.api.price.PriceData;
import com.musicslayer.cryptobuddy.asset.Asset;
import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.asset.fiat.Fiat;
import com.musicslayer.cryptobuddy.crash.CrashDialogInterface;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.settings.setting.PriceDisplaySetting;
import com.musicslayer.cryptobuddy.transaction.AssetQuantity;
import com.musicslayer.cryptobuddy.transaction.AssetPrice;
import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.serialize.Serialization;
import com.musicslayer.cryptobuddy.util.HashMapUtil;
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
        ssvPrimary.setCompleteOptions();
        ssvPrimary.chooseCoin();

        SelectAndSearchView ssvSecondary = findViewById(R.id.crypto_converter_dialog_secondarySelectAndSearchView);
        ssvSecondary.setIncludesFiat(false);
        ssvSecondary.setIncludesCoin(true);
        ssvSecondary.setIncludesToken(true);
        ssvSecondary.setCompleteOptions();
        ssvSecondary.chooseCoin();

        SelectAndSearchView fssv = findViewById(R.id.crypto_converter_dialog_fiatSelectAndSearchView);
        fssv.setIncludesFiat(true);
        fssv.setIncludesCoin(false);
        fssv.setIncludesToken(false);
        fssv.setCompleteOptions();
        fssv.chooseFiat();

        ProgressDialogFragment progressDialogFragment = ProgressDialogFragment.newInstance(ProgressDialog.class);
        progressDialogFragment.setOnShowListener(new CrashDialogInterface.CrashOnShowListener(this.activity) {
            @Override
            public void onShowImpl(DialogInterface dialog) {
                ProgressDialogFragment.updateProgressTitle("Performing Conversion...");

                Fiat priceFiat = (Fiat)fssv.getChosenAsset();

                ArrayList<Asset> assetArrayList = new ArrayList<>();
                assetArrayList.add(cryptoPrimary);
                assetArrayList.add(cryptoSecondary);

                CryptoPrice cryptoPrice = new CryptoPrice(assetArrayList, priceFiat);

                PriceData priceData = PriceData.getPriceData(cryptoPrice);
                ProgressDialogFragment.setValue(Serialization.serialize(priceData));
            }
        });

        progressDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this.activity) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                PriceData priceData = Serialization.deserialize(ProgressDialogFragment.getValue(), PriceData.class);

                boolean isComplete = false;

                if(priceData.isPriceComplete()) {
                    AssetQuantity primaryPriceAssetQuantity = HashMapUtil.getValueFromMap(priceData.priceHashMap, cryptoPrimary);
                    AssetQuantity secondaryPriceAssetQuantity = HashMapUtil.getValueFromMap(priceData.priceHashMap, cryptoSecondary);

                    if(primaryPriceAssetQuantity != null && secondaryPriceAssetQuantity != null) {
                        AssetQuantity primaryAssetQuantity = new AssetQuantity(E_PRIMARYASSET.getTextString(), cryptoPrimary);
                        AssetPrice primaryAssetPrice = new AssetPrice(new AssetQuantity("1", cryptoPrimary), primaryPriceAssetQuantity);
                        AssetPrice secondaryAssetPrice = new AssetPrice(new AssetQuantity("1", cryptoSecondary), secondaryPriceAssetQuantity);
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
                        isComplete = true;
                    }
                }

                if(!isComplete) {
                    T.setText("");
                    ToastUtil.showToast(activity,"incomplete_price_data");
                }
            }
        });
        progressDialogFragment.restoreListeners(this.activity, "progress");

        Button B_CONVERT = findViewById(R.id.crypto_converter_dialog_convertButton);
        B_CONVERT.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                boolean isValid = E_PRIMARYASSET.test();

                cryptoPrimary = (Crypto)ssvPrimary.getChosenAsset();
                cryptoSecondary = (Crypto)ssvSecondary.getChosenAsset();

                if(ssvPrimary.getChosenAsset() == null || ssvSecondary.getChosenAsset() == null || fssv.getChosenAsset() == null) {
                    ToastUtil.showToast(activity,"must_choose_assets");
                }
                else if(!isValid) {
                    ToastUtil.showToast(activity,"must_fill_inputs");
                }
                else {
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
