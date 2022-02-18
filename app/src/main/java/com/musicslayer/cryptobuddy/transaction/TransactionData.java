package com.musicslayer.cryptobuddy.transaction;

import com.musicslayer.cryptobuddy.api.price.PriceData;
import com.musicslayer.cryptobuddy.asset.Asset;
import com.musicslayer.cryptobuddy.rich.RichStringBuilder;
import com.musicslayer.cryptobuddy.data.Serialization;

import java.util.ArrayList;
import java.util.HashMap;

public class TransactionData {
    final public ArrayList<Transaction> transactionArrayList;
    final public HashMap<Asset, AssetAmount> netTransactionsMap;

    final public AssetQuantityData assetQuantityData;

    public TransactionData(ArrayList<Transaction> transactionArrayList) {
        this.transactionArrayList = transactionArrayList;

        netTransactionsMap = Transaction.resolveAssets(transactionArrayList);
        assetQuantityData = new AssetQuantityData(netTransactionsMap);
    }

    public String getAllTransactionInfo(PriceData priceData, boolean isRich) {
        RichStringBuilder s = new RichStringBuilder(isRich);

        if(transactionArrayList == null || transactionArrayList.isEmpty()) {
            s.appendRich("No transactions found.");
        }
        else {
            s.appendRich("Transactions:");
            s.appendRich("\n").appendRich(Serialization.serializeArrayList(transactionArrayList));

            s.appendRich("\n\n").appendRich("Net Transaction Sums:");

            HashMap<Asset, AssetQuantity> priceMap = priceData == null ? null : priceData.priceHashMap;
            s.append(assetQuantityData.getAssetQuantityInfo(priceMap, isRich));

            if(priceData != null) {
                s.appendRich("\n\nPrice Data Source = ").appendRich(priceData.priceAPI_price.getDisplayName());
                s.appendRich("\nPrice Data Timestamp = ").appendRich(priceData.timestamp_price.toString());
            }
        }

        return s.toString();
    }

    public String getNetSumsInfo(PriceData priceData, boolean isRich) {
        RichStringBuilder s = new RichStringBuilder(isRich);

        if(transactionArrayList == null || transactionArrayList.isEmpty()) {
            s.appendRich("No transactions found.");
        }
        else {
            s.appendRich("Net Transaction Sums:");

            HashMap<Asset, AssetQuantity> priceMap = priceData == null ? null : priceData.priceHashMap;
            s.append(assetQuantityData.getAssetQuantityInfo(priceMap, isRich));

            if(priceData != null) {
                s.appendRich("\n\nPrice Data Source = ").appendRich(priceData.priceAPI_price.getDisplayName());
                s.appendRich("\nPrice Data Timestamp = ").appendRich(priceData.timestamp_price.toString());
            }
        }

        return s.toString();
    }
}