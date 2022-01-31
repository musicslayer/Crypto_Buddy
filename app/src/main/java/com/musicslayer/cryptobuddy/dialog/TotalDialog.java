package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.api.price.BulkPriceData;
import com.musicslayer.cryptobuddy.asset.Asset;
import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.asset.fiat.Fiat;
import com.musicslayer.cryptobuddy.asset.fiat.USD;
import com.musicslayer.cryptobuddy.crash.CrashDialogInterface;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.state.StateObj;
import com.musicslayer.cryptobuddy.transaction.AssetAmount;
import com.musicslayer.cryptobuddy.transaction.AssetPrice;
import com.musicslayer.cryptobuddy.transaction.AssetQuantity;
import com.musicslayer.cryptobuddy.transaction.Transaction;
import com.musicslayer.cryptobuddy.serialize.Serialization;
import com.musicslayer.cryptobuddy.util.HashMapUtil;
import com.musicslayer.cryptobuddy.util.ToastUtil;

import java.util.ArrayList;
import java.util.HashMap;

public class TotalDialog extends BaseDialog {
    ArrayList<Transaction> filteredTransactionArrayList;
    HashMap<Asset, AssetAmount> deltaMap;
    HashMap<Asset, AssetAmount> priceMap = new HashMap<>();

    public TotalDialog(Activity activity) {
        super(activity);
        this.filteredTransactionArrayList = StateObj.filteredTransactionArrayList;
    }

    public int getBaseViewID() {
        return R.id.total_dialog;
    }

    public void createLayout(Bundle savedInstanceState) {
        setContentView(R.layout.dialog_total);

        deltaMap = Transaction.resolveAssets(filteredTransactionArrayList);

        ProgressDialogFragment progressDialogFragment = ProgressDialogFragment.newInstance(ProgressDialog.class);
        progressDialogFragment.setOnShowListener(new CrashDialogInterface.CrashOnShowListener(this.activity) {
            @Override
            public void onShowImpl(DialogInterface dialog) {
                ProgressDialogFragment.updateProgressTitle("Calculating Total...");

                HashMap<Asset, AssetAmount> newPriceMap = new HashMap<>();

                ArrayList<Asset> keySet = new ArrayList<>(deltaMap.keySet());
                Asset.sortAscendingByType(keySet);

                // For now, USD is the only fiat, and it's price is 1 by definition.
                // Take it out of the array and deal with it ourselves.
                ArrayList<Crypto> cryptoKeySet = new ArrayList<>();
                for(Asset asset : keySet) {
                    if(asset instanceof Fiat) {
                        HashMapUtil.putValueInMap(newPriceMap, asset, new AssetAmount("1"));
                    }
                    else if(asset instanceof Crypto) {
                        cryptoKeySet.add((Crypto)asset);
                    }
                }

                BulkPriceData bulkPriceData = BulkPriceData.getBulkPriceData(cryptoKeySet);
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

                if(newPriceMap.size() != deltaMap.size()) {
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

        Button B_PRICES = findViewById(R.id.total_dialog_priceButton);
        B_PRICES.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                if(deltaMap.isEmpty()) {
                    ToastUtil.showToast(activity, "no_transactions_found");
                    return;
                }

                progressDialogFragment.show(TotalDialog.this.activity, "progress");
            }
        });

        updateLayout();
    }

    public void updateLayout() {
        AssetAmount grandTotal = new AssetAmount("0");
        StringBuilder s = new StringBuilder("Net Amounts:");

        if(deltaMap.isEmpty()) {
            s = new StringBuilder("No assets found.");
        }

        ArrayList<Asset> keySet = new ArrayList<>(deltaMap.keySet());
        Asset.sortAscendingByType(keySet);

        for(Asset asset : keySet) {
            AssetAmount amount = HashMapUtil.getValueFromMap(deltaMap, asset);
            AssetQuantity assetQuantity = new AssetQuantity(amount, asset);

            s.append("\n").append(assetQuantity);

            AssetAmount price = HashMapUtil.getValueFromMap(priceMap, asset);
            if(price != null) {
                AssetPrice assetPrice = new AssetPrice(new AssetQuantity("1", asset), new AssetQuantity(price, new USD()));
                AssetQuantity convertedAssetQuantity = assetQuantity.convert(assetPrice);

                grandTotal = grandTotal.add(convertedAssetQuantity.assetAmount);
                s.append(" = ").append(convertedAssetQuantity);
            }
            else {
                s.append(" = ?");
            }
        }

        String titleString = "Grand Total";
        if(!priceMap.isEmpty()) {
            s.append("\n\nData Source = CoinGecko API V3");

            AssetQuantity grandTotalAssetQuantity = new AssetQuantity(grandTotal, new USD());
            titleString = titleString + " = " + grandTotalAssetQuantity;
        }

        Toolbar toolbar = findViewById(R.id.total_dialog_toolbar);
        toolbar.setTitle(titleString);
        toolbar.setSubtitle("(Does not include filtered data)");

        TextView T = findViewById(R.id.total_dialog_textView);
        T.setText(s.toString());
    }

    @Override
    public Bundle onSaveInstanceStateImpl(Bundle bundle) {
        bundle.putSerializable("priceMap", priceMap);
        return bundle;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onRestoreInstanceStateImpl(Bundle bundle) {
        if(bundle != null) {
            priceMap = (HashMap<Asset, AssetAmount>)bundle.getSerializable("priceMap");
        }
    }
}