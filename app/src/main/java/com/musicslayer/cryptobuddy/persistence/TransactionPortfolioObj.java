package com.musicslayer.cryptobuddy.persistence;

import com.musicslayer.cryptobuddy.asset.Asset;
import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;
import com.musicslayer.cryptobuddy.asset.fiat.Fiat;
import com.musicslayer.cryptobuddy.asset.tokenmanager.TokenManager;
import com.musicslayer.cryptobuddy.transaction.Action;
import com.musicslayer.cryptobuddy.transaction.AssetAmount;
import com.musicslayer.cryptobuddy.transaction.AssetQuantity;
import com.musicslayer.cryptobuddy.transaction.Timestamp;
import com.musicslayer.cryptobuddy.transaction.Transaction;
import com.musicslayer.cryptobuddy.serialize.Serialization;

import java.util.ArrayList;
import java.util.Date;

public class TransactionPortfolioObj implements Serialization.SerializableToJSON {
    public String name;
    public ArrayList<Transaction> transactionArrayList = new ArrayList<>();

    public TransactionPortfolioObj(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object other) {
        return (other instanceof TransactionPortfolioObj) && name.equals(((TransactionPortfolioObj)other).name);
    }

    public void addData(Transaction transaction) {
        transactionArrayList.add(transaction);
    }

    public void removeData(Transaction transaction) {
        transactionArrayList.remove(transaction);
    }

    public String serializationVersion() { return "1"; }

    public String serializeToJSON() throws org.json.JSONException {
        return new Serialization.JSONObjectWithNull()
            .put("name", Serialization.string_serialize(name))
            .put("transactionArrayList", new Serialization.JSONArrayWithNull(Serialization.serializeArrayList(transactionArrayList)))
            .toStringOrNull();
    }

    public static TransactionPortfolioObj deserializeFromJSON1(String s) throws org.json.JSONException {
        Serialization.JSONObjectWithNull o = new Serialization.JSONObjectWithNull(s);
        String name = Serialization.string_deserialize(o.getString("name"));
        ArrayList<Transaction> transactionArrayList = Serialization.deserializeArrayList(o.getJSONArrayString("transactionArrayList"), Transaction.class);

        TransactionPortfolioObj transactionPortfolioObj = new TransactionPortfolioObj(name);
        transactionPortfolioObj.transactionArrayList = transactionArrayList;

        return transactionPortfolioObj;
    }

    // TODO This should be removed soon.
    public static TransactionPortfolioObj deserializeFromJSON0(String s) {
        String[] sArray = s.split("\n");

        String name = sArray[0];
        TransactionPortfolioObj transactionPortfolioObj = new TransactionPortfolioObj(name);

        // A crypto may no longer exist, especially if it is a token.
        for(int i = 1; i < sArray.length; i++) {
            String[] transactionStringArray = sArray[i].split("\\|");

            Action action = new Action(transactionStringArray[0]);

            Asset asset = getAsset(transactionStringArray[1], transactionStringArray[2]);
            AssetQuantity actionedAssetQuantity = new AssetQuantity(new AssetAmount(transactionStringArray[3]), asset);

            AssetQuantity otherAssetQuantity;
            if(transactionStringArray[4].isEmpty() && transactionStringArray[5].isEmpty()) {
                otherAssetQuantity = null;
            }
            else {
                Asset otherAsset = getAsset(transactionStringArray[4], transactionStringArray[5]);
                otherAssetQuantity = new AssetQuantity(new AssetAmount(transactionStringArray[6]), otherAsset);
            }

            Timestamp timestamp = new Timestamp(legacyDeserializeDate(transactionStringArray[7]));
            String info = transactionStringArray[8];

            transactionPortfolioObj.addData(new Transaction(action, actionedAssetQuantity, otherAssetQuantity, timestamp, info));
        }

        return transactionPortfolioObj;
    }

    public static Asset getAsset(String tokenType, String key) {
        if("!FIAT!".equals(tokenType)) {
            return Fiat.getFiatFromKey(key);
        }
        else if("!COIN!".equals(tokenType)) {
            return Coin.getCoinFromKey(key);
        }
        else {
            return TokenManager.getTokenManagerFromTokenType(tokenType).getToken(key, null, null, 0, null);
        }
    }

    public static Date legacyDeserializeDate(String s) {
        // Input string should parse to be a long value.
        if(s == null || "null".equals(s)) {
            return null;
        }
        else {
            return new Date(Long.parseLong(s));
        }
    }
}
