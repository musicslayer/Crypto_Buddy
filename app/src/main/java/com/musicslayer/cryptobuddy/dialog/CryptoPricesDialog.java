package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.crash.CrashOnClickListener;
import com.musicslayer.cryptobuddy.crash.CrashOnDismissListener;
import com.musicslayer.cryptobuddy.crash.CrashOnShowListener;
import com.musicslayer.cryptobuddy.transaction.AssetQuantity;
import com.musicslayer.cryptobuddy.transaction.AssetPrice;
import com.musicslayer.cryptobuddy.api.price.PriceData;
import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.persistence.Settings;
import com.musicslayer.cryptobuddy.serialize.Serialization;
import com.musicslayer.cryptobuddy.util.ToastUtil;
import com.musicslayer.cryptobuddy.view.SelectAndSearchView;

public class CryptoPricesDialog extends BaseDialog {
    PriceData priceData;
    Crypto crypto;

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
        ssv.setIncludesFiat(false);
        ssv.setOptionsCoin();

        ProgressDialogFragment progressDialogFragment = ProgressDialogFragment.newInstance(ProgressDialog.class);
        progressDialogFragment.setOnShowListener(new CrashOnShowListener(this.activity) {
            @Override
            public void onShowImpl(DialogInterface dialog) {
                priceData = PriceData.getPriceData(crypto);
            }
        });

        progressDialogFragment.setOnDismissListener(new CrashOnDismissListener(this.activity) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(priceData.isComplete()) {
                    AssetPrice assetPrice = new AssetPrice(new AssetQuantity("1", priceData.crypto), priceData.price);
                    AssetQuantity marketCapAssetQuantity = priceData.marketCap;

                    String text = "Forward Price = " + assetPrice.toString();
                    if("ForwardBackward".equals(Settings.setting_price)) {
                        text = text + "\nBackward Price = " + assetPrice.reverseAssetPrice().toString();
                    }
                    text = text + "\nMarket Cap = " + marketCapAssetQuantity.toString() + "\nData Source = CoinGecko API V3";

                    T.setText(text);
                }
                else {
                    T.setText("");
                    ToastUtil.showToast(activity,"no_price_data");
                }
            }
        });
        progressDialogFragment.restoreListeners(this.activity, "progress");

        Button B = findViewById(R.id.crypto_prices_dialog_button);
        B.setOnClickListener(new CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                crypto = (Crypto)ssv.getChosenAsset();
                progressDialogFragment.show(CryptoPricesDialog.this.activity, "progress");
            }
        });
    }

    @Override
    public Bundle onSaveInstanceStateImpl(Bundle bundle) {
        String priceData_s = priceData == null ? "{}" : Serialization.serialize(priceData);
        bundle.putString("priceData", priceData_s);

        String crypto_s = crypto == null ? "{}" : Serialization.serialize(crypto);
        bundle.putString("crypto", crypto_s);

        return bundle;
    }

    @Override
    public void onRestoreInstanceStateImpl(Bundle bundle) {
        if(bundle != null) {
            String priceData_s = bundle.getString("priceData");
            priceData = "{}".equals(priceData_s) ? null : Serialization.deserialize(priceData_s, PriceData.class);

            String crypto_s = bundle.getString("crypto");
            crypto = "{}".equals(crypto_s) ? null : Serialization.deserialize(crypto_s, Crypto.class);
        }
    }
}
