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
import com.musicslayer.cryptobuddy.api.price.BulkPriceData;
import com.musicslayer.cryptobuddy.asset.Asset;
import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.asset.fiat.Fiat;
import com.musicslayer.cryptobuddy.crash.CrashAdapterView;
import com.musicslayer.cryptobuddy.crash.CrashDialogInterface;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.serialize.Serialization;
import com.musicslayer.cryptobuddy.state.StateObj;
import com.musicslayer.cryptobuddy.transaction.AssetAmount;
import com.musicslayer.cryptobuddy.transaction.AssetQuantity;
import com.musicslayer.cryptobuddy.util.HashMapUtil;
import com.musicslayer.cryptobuddy.util.ToastUtil;
import com.musicslayer.cryptobuddy.view.BorderedSpinnerView;
import com.musicslayer.cryptobuddy.view.SelectAndSearchView;

import java.util.ArrayList;
import java.util.HashMap;

public class ExchangeInfoDialog extends BaseDialog {
    ArrayList<CryptoExchange> cryptoExchangeArrayList;
    HashMap<CryptoExchange, ExchangeData> exchangeDataMap;
    int cryptoExchangeIdx;

    ArrayList<AssetQuantity> deltaArray = new ArrayList<>();
    HashMap<Asset, AssetAmount> priceMap = new HashMap<>();

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

        SelectAndSearchView fssv = findViewById(R.id.exchange_info_dialog_fiatSelectAndSearchView);
        fssv.setIncludesFiat(true);
        fssv.setIncludesCoin(false);
        fssv.setIncludesToken(false);
        fssv.setOptionsFiat();

        ProgressDialogFragment progressDialogFragment = ProgressDialogFragment.newInstance(ProgressDialog.class);
        progressDialogFragment.setOnShowListener(new CrashDialogInterface.CrashOnShowListener(this.activity) {
            @Override
            public void onShowImpl(DialogInterface dialog) {
                ProgressDialogFragment.updateProgressTitle("Calculating Total...");

                HashMap<Asset, AssetAmount> newPriceMap = new HashMap<>();

                HashMap<Asset, AssetAmount> deltaMap = new HashMap<>();
                for(AssetQuantity assetQuantity : deltaArray) {
                    HashMapUtil.putValueInMap(deltaMap, assetQuantity.asset, assetQuantity.assetAmount);
                }

                ArrayList<Asset> keySet = new ArrayList<>(deltaMap.keySet());
                Asset.sortAscendingByType(keySet);

                // Cryptos and Fiat need to be converted by different means.
                ArrayList<Crypto> cryptoKeySet = new ArrayList<>();
                for(Asset asset : keySet) {
                    if(asset instanceof Fiat) {
                        // TODO Actually perform fiat conversions. (Use Bitcoin as an intermediary?)
                        HashMapUtil.putValueInMap(newPriceMap, asset, new AssetAmount("1"));
                    }
                    else if(asset instanceof Crypto) {
                        cryptoKeySet.add((Crypto)asset);
                    }
                }

                Fiat priceFiat = (Fiat)fssv.getChosenAsset();
                BulkPriceData bulkPriceData = BulkPriceData.getBulkPriceData(cryptoKeySet, priceFiat);
                if(bulkPriceData.isPriceComplete()) {
                    HashMap<Crypto, AssetQuantity> priceHashMap = bulkPriceData.priceHashMap;
                    for(Crypto crypto : priceHashMap.keySet()) {
                        AssetQuantity price = HashMapUtil.getValueFromMap(priceHashMap, crypto);
                        if(price != null) {
                            HashMapUtil.putValueInMap(newPriceMap, crypto, price.assetAmount);
                        }
                    }
                }

                ProgressDialogFragment.setValue(Serialization.serializeHashMap(newPriceMap));
            }
        });

        progressDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this.activity) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                HashMap<Asset, AssetAmount> newPriceMap = Serialization.deserializeHashMap(ProgressDialogFragment.getValue(), Asset.class, AssetAmount.class);

                if(newPriceMap.size() != deltaArray.size()) {
                    ToastUtil.showToast(activity,"incomplete_price_data");
                }

                priceMap.clear();
                for(Asset asset : newPriceMap.keySet()) {
                    HashMapUtil.putValueInMap(priceMap, asset, newPriceMap.get(asset));
                }

                updateLayout();
            }
        });
        progressDialogFragment.restoreListeners(this.activity, "progress");

        Button B_PRICES = findViewById(R.id.exchange_info_dialog_priceButton);
        B_PRICES.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                if(deltaArray.isEmpty()) {
                    ToastUtil.showToast(activity, "no_balances_found");
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

                CryptoExchange cryptoExchange = cryptoExchangeArrayList.get(pos);
                ExchangeData exchangeData = HashMapUtil.getValueFromMap(exchangeDataMap, cryptoExchange);

                deltaArray = exchangeData.currentBalanceArrayList;
                if(deltaArray == null) {
                    deltaArray = new ArrayList<>();
                }

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
        SelectAndSearchView fssv = findViewById(R.id.exchange_info_dialog_fiatSelectAndSearchView);

        Fiat priceFiat = (Fiat)fssv.getChosenAsset();
        T.setText(Html.fromHtml(exchangeData.getInfoString(priceMap, priceFiat, true)));
    }

    @Override
    public Bundle onSaveInstanceStateImpl(Bundle bundle) {
        bundle.putInt("cryptoExchangeIdx", cryptoExchangeIdx);
        bundle.putSerializable("priceMap", priceMap);
        return bundle;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onRestoreInstanceStateImpl(Bundle bundle) {
        if(bundle != null) {
            priceMap = (HashMap<Asset, AssetAmount>)bundle.getSerializable("priceMap");
            cryptoExchangeIdx = bundle.getInt("cryptoExchangeIdx");
        }
    }
}