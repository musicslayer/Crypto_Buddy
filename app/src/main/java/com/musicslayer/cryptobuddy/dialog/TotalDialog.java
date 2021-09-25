package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.api.price.PriceData;
import com.musicslayer.cryptobuddy.asset.Asset;
import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.asset.fiat.Fiat;
import com.musicslayer.cryptobuddy.asset.fiat.USD;
import com.musicslayer.cryptobuddy.transaction.AssetAmount;
import com.musicslayer.cryptobuddy.transaction.AssetPrice;
import com.musicslayer.cryptobuddy.transaction.AssetQuantity;
import com.musicslayer.cryptobuddy.transaction.Transaction;
import com.musicslayer.cryptobuddy.util.Toast;

import java.util.ArrayList;
import java.util.HashMap;

public class TotalDialog extends BaseDialog {
    ArrayList<Transaction> transactionArrayList;
    HashMap<Asset, AssetAmount> deltaMap;
    HashMap<Asset, AssetAmount> priceMap = new HashMap<>();

    final HashMap<Asset, AssetAmount>[] newPriceMap = new HashMap[1];

    public TotalDialog(Activity activity, ArrayList<Transaction> transactionArrayList) {
        super(activity);
        this.transactionArrayList = transactionArrayList;
    }

    public int getBaseViewID() {
        return R.id.total_dialog;
    }

    public void createLayout() {
        setContentView(R.layout.dialog_total);

        deltaMap = Transaction.resolveAssets(transactionArrayList);

        ProgressDialogFragment progressDialogFragment = ProgressDialogFragment.newInstance(ProgressDialog.class);
        progressDialogFragment.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                newPriceMap[0] = new HashMap<>();

                ArrayList<Asset> keySet = new ArrayList<>(deltaMap.keySet());
                Asset.sortAscendingByType(keySet);

                for(Asset asset : keySet) {
                    if(((ProgressDialog)dialog).isCancelled) { return; }

                    if(asset instanceof Fiat) {
                        // For now, USD is the only fiat, and it's price is 1 by definition.
                        newPriceMap[0].put(asset, new AssetAmount("1"));
                    }
                    else if(asset instanceof Crypto) {
                        Crypto crypto = (Crypto)asset;
                        PriceData priceData = PriceData.getPriceData(crypto);

                        // We only need price data.
                        if(priceData.priceAPI_usdPrice != null) {
                            newPriceMap[0].put(crypto, new AssetAmount(priceData.usdPrice));
                        }
                    }
                }
            }
        });

        progressDialogFragment.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if(newPriceMap[0].size() != deltaMap.size()) {
                    Toast.showToast("no_price_data");
                }

                priceMap.clear();
                for(Asset asset : newPriceMap[0].keySet()) {
                    priceMap.put(asset, newPriceMap[0].get(asset));
                }

                updateLayout();
            }
        });
        progressDialogFragment.restoreListeners(this.activity, "progress");

        Button B_PRICES = findViewById(R.id.total_dialog_priceButton);
        B_PRICES.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
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
            AssetAmount amount = deltaMap.get(asset);
            AssetQuantity assetQuantity = new AssetQuantity(amount, asset);

            s.append("\n").append(assetQuantity.toString());

            AssetAmount price = priceMap.get(asset);
            if(price != null) {
                AssetPrice assetPrice = new AssetPrice(new AssetQuantity("1", asset), new AssetQuantity(price, new USD()));
                AssetQuantity convertedAssetQuantity = assetQuantity.convert(assetPrice);

                grandTotal = grandTotal.add(convertedAssetQuantity.assetAmount);
                s.append(" = ").append(convertedAssetQuantity.toString());
            }
            else {
                s.append(" = ?");
            }
        }

        String titleString = "Grand Total";
        if(!priceMap.isEmpty()) {
            s.append("\n\nData Source = CoinGecko API V3");

            AssetQuantity grandTotalAssetQuantity = new AssetQuantity(grandTotal, new USD());
            titleString = titleString + " = " + grandTotalAssetQuantity.toString();
        }

        Toolbar toolbar = findViewById(R.id.total_dialog_toolbar);
        toolbar.setTitle(titleString);
        toolbar.setSubtitle("(Does not include filtered data)");

        TextView T = findViewById(R.id.total_dialog_textView);
        T.setText(s.toString());
    }

    @Override
    public Bundle onSaveInstanceState() {
        super.onSaveInstanceState();

        Bundle bundle = super.onSaveInstanceState();
        bundle.putSerializable("priceMap", priceMap);
        return bundle;
    }

    @Override
    public void onRestoreInstanceState(Bundle bundle) {
        if(bundle != null) {
            priceMap = (HashMap<Asset, AssetAmount>)bundle.getSerializable("priceMap");
        }

        super.onRestoreInstanceState(bundle);
    }
}