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
}
