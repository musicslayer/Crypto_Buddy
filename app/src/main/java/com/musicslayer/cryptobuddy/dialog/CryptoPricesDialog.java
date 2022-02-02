package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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
import com.musicslayer.cryptobuddy.util.ToastUtil;
import com.musicslayer.cryptobuddy.view.SelectAndSearchView;

import java.util.ArrayList;

public class CryptoPricesDialog extends BaseDialog {
    Crypto crypto;

    public CryptoPricesDialog(Activity activity) {
        super(activity);
    }

    public int getBaseViewID() {
        return R.id.crypto_prices_dialog;
    }

    public void createLayout(Bundle savedInstanceState) {
        setContentView(R.layout.dialog_crypto_prices);

        TextView T = findViewById(R.id.crypto_prices_dialog_textView);
        SelectAndSearchView ssv = findViewById(R.id.crypto_prices_dialog_selectAndSearchView);
        ssv.setIncludesFiat(false);
        ssv.setIncludesCoin(true);
        ssv.setIncludesToken(true);
        ssv.setOptionsCoin();

        SelectAndSearchView fssv = findViewById(R.id.crypto_prices_dialog_fiatSelectAndSearchView);
        fssv.setIncludesFiat(true);
        fssv.setIncludesCoin(false);
        fssv.setIncludesToken(false);
        fssv.setOptionsFiat();

        ProgressDialogFragment progressDialogFragment = ProgressDialogFragment.newInstance(ProgressDialog.class);
        progressDialogFragment.setOnShowListener(new CrashDialogInterface.CrashOnShowListener(this.activity) {
            @Override
            public void onShowImpl(DialogInterface dialog) {
                ProgressDialogFragment.updateProgressTitle("Obtaining Price...");

                Fiat priceFiat = (Fiat)fssv.getChosenAsset();

                ArrayList<Asset> assetArrayList = new ArrayList<>();
                assetArrayList.add(crypto);

                CryptoPrice cryptoPrice = new CryptoPrice(assetArrayList, priceFiat);

                PriceData priceData = PriceData.getAllData(cryptoPrice);
                ProgressDialogFragment.setValue(Serialization.serialize(priceData));
            }
        });

        progressDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this.activity) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                PriceData priceData = Serialization.deserialize(ProgressDialogFragment.getValue(), PriceData.class);

                if(priceData.isComplete()) {
                    AssetPrice assetPrice = new AssetPrice(new AssetQuantity("1", crypto), priceData.priceHashMap.get(crypto));
                    AssetQuantity marketCapAssetQuantity = priceData.marketCapHashMap.get(crypto);

                    String text = "Forward Price = " + assetPrice;
                    if("ForwardBackward".equals(PriceDisplaySetting.value)) {
                        text = text + "\nBackward Price = " + assetPrice.reverseAssetPrice().toString();
                    }
                    text = text + "\nMarket Cap = " + marketCapAssetQuantity.toString() + "\nData Source = CoinGecko API V3";

                    T.setText(text);
                }
                else {
                    T.setText("");
                    ToastUtil.showToast(activity,"incomplete_price_data");
                }
            }
        });
        progressDialogFragment.restoreListeners(this.activity, "progress");

        Button B = findViewById(R.id.crypto_prices_dialog_button);
        B.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                crypto = (Crypto)ssv.getChosenAsset();
                progressDialogFragment.show(CryptoPricesDialog.this.activity, "progress");
            }
        });
    }

    @Override
    public Bundle onSaveInstanceStateImpl(Bundle bundle) {
        bundle.putParcelable("crypto", crypto);

        return bundle;
    }

    @Override
    public void onRestoreInstanceStateImpl(Bundle bundle) {
        if(bundle != null) {
            crypto = bundle.getParcelable("crypto");
        }
    }
}
