package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.api.exchange.CryptoExchange;
import com.musicslayer.cryptobuddy.api.exchange.ExchangeData;
import com.musicslayer.cryptobuddy.api.price.CryptoPrice;
import com.musicslayer.cryptobuddy.api.price.PriceData;
import com.musicslayer.cryptobuddy.asset.Asset;
import com.musicslayer.cryptobuddy.asset.fiat.Fiat;
import com.musicslayer.cryptobuddy.crash.CrashAdapterView;
import com.musicslayer.cryptobuddy.crash.CrashDialogInterface;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.data.bridge.Serialization;
import com.musicslayer.cryptobuddy.state.StateObj;
import com.musicslayer.cryptobuddy.transaction.AssetAmount;
import com.musicslayer.cryptobuddy.transaction.AssetQuantity;
import com.musicslayer.cryptobuddy.util.HashMapUtil;
import com.musicslayer.cryptobuddy.util.ToastUtil;
import com.musicslayer.cryptobuddy.view.BorderedSpinnerView;
import com.musicslayer.cryptobuddy.view.asset.SelectAndSearchView;

import java.util.ArrayList;
import java.util.HashMap;

public class ExchangeInfoDialog extends BaseDialog {
    ArrayList<CryptoExchange> cryptoExchangeArrayList;
    HashMap<CryptoExchange, ExchangeData> exchangeDataMap;
    int cryptoExchangeIdx;

    public ExchangeInfoDialog(Activity activity, ArrayList<CryptoExchange> cryptoExchangeArrayList) {
        super(activity);
        this.cryptoExchangeArrayList = cryptoExchangeArrayList;
        this.exchangeDataMap = StateObj.exchangeDataMap;
    }

    public int getBaseViewID() {
        return R.id.exchange_info_dialog;
    }

    public void createLayout(Bundle savedInstanceState) {
        setContentView(R.layout.dialog_exchange_info);

        if(savedInstanceState == null) {
            StateObj.priceData = null;
        }

        SelectAndSearchView fssv = findViewById(R.id.exchange_info_dialog_fiatSelectAndSearchView);
        fssv.setIncludesFiat(true);
        fssv.setIncludesCoin(false);
        fssv.setIncludesToken(false);
        fssv.setCompleteOptions();
        fssv.chooseFiat("BASE");

        ProgressDialogFragment progressDialogFragment = ProgressDialogFragment.newInstance(ProgressDialog.class);
        progressDialogFragment.setOnShowListener(new CrashDialogInterface.CrashOnShowListener(this.activity) {
            @Override
            public void onShowImpl(DialogInterface dialog) {
                ProgressDialogFragment.updateProgressTitle("Retrieving Fiat Values...");

                CryptoExchange cryptoExchange = cryptoExchangeArrayList.get(cryptoExchangeIdx);
                ExchangeData exchangeData = HashMapUtil.getValueFromMap(exchangeDataMap, cryptoExchange);
                ArrayList<AssetQuantity> deltaArray = exchangeData.currentBalanceArrayList;

                HashMap<Asset, AssetAmount> deltaMap = new HashMap<>();
                for(AssetQuantity assetQuantity : deltaArray) {
                    HashMapUtil.putValueInMap(deltaMap, assetQuantity.asset, assetQuantity.assetAmount);
                }

                ArrayList<Asset> assetKeySet = new ArrayList<>(deltaMap.keySet());
                Fiat priceFiat = (Fiat)fssv.getChosenAsset();
                CryptoPrice cryptoPrice = new CryptoPrice(assetKeySet, priceFiat);

                PriceData newPriceData = PriceData.getPriceData(cryptoPrice);

                ProgressDialogFragment.setValue(Serialization.serialize(newPriceData, PriceData.class));
            }
        });

        progressDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this.activity) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                PriceData newPriceData = Serialization.deserialize(ProgressDialogFragment.getValue(), PriceData.class);

                if(!newPriceData.isPriceFull()) {
                    ToastUtil.showToast("incomplete_price_data");
                }

                StateObj.priceData = newPriceData;

                updateLayout();
            }
        });
        progressDialogFragment.restoreListeners(this.activity, "progress");

        Button B_PRICES = findViewById(R.id.exchange_info_dialog_priceButton);
        B_PRICES.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                CryptoExchange cryptoExchange = cryptoExchangeArrayList.get(cryptoExchangeIdx);
                ExchangeData exchangeData = HashMapUtil.getValueFromMap(exchangeDataMap, cryptoExchange);
                ArrayList<AssetQuantity> deltaArray = exchangeData.currentBalanceArrayList;

                if(deltaArray.isEmpty()) {
                    ToastUtil.showToast("no_balances_found");
                    return;
                }
                else if(fssv.getChosenAsset() == null) {
                    ToastUtil.showToast("must_choose_assets");
                    return;
                }

                progressDialogFragment.show(activity, "progress");
            }
        });

        ArrayList<String> options = new ArrayList<>();
        for(CryptoExchange cryptoExchange : cryptoExchangeArrayList) {
            options.add(cryptoExchange.toString());
        }

        TextView T = findViewById(R.id.exchange_info_dialog_textView);

        BorderedSpinnerView bsv = findViewById(R.id.exchange_info_dialog_spinner);
        bsv.setOptions(options);
        bsv.setOnItemSelectedListener(new CrashAdapterView.CrashOnItemSelectedListener(this.activity) {
            public void onNothingSelectedImpl(AdapterView<?> parent) {}
            public void onItemSelectedImpl(AdapterView<?> parent, View view, int pos, long id) {
                cryptoExchangeIdx = pos;
                updateLayout();
            }
        });

        if(cryptoExchangeArrayList.size() == 1) {
            bsv.setVisibility(View.GONE);
        }

        if(cryptoExchangeArrayList.size() == 0) {
            bsv.setVisibility(View.GONE);
            T.setText("No exchanges found.");
        }
    }

    public void updateLayout() {
        CryptoExchange cryptoExchange = cryptoExchangeArrayList.get(cryptoExchangeIdx);
        ExchangeData exchangeData = HashMapUtil.getValueFromMap(exchangeDataMap, cryptoExchange);

        TextView T = findViewById(R.id.exchange_info_dialog_textView);
        T.setText(Html.fromHtml(exchangeData.getInfoString(StateObj.priceData, true)));
    }

    @Override
    public Bundle onSaveInstanceStateImpl(Bundle bundle) {
        bundle.putInt("cryptoExchangeIdx", cryptoExchangeIdx);
        return bundle;
    }

    @Override
    public void onRestoreInstanceStateImpl(Bundle bundle) {
        if(bundle != null) {
            cryptoExchangeIdx = bundle.getInt("cryptoExchangeIdx");
        }
    }
}