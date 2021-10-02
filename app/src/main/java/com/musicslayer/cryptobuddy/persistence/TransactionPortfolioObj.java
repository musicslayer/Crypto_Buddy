package com.musicslayer.cryptobuddy.persistence;

import com.musicslayer.cryptobuddy.transaction.Transaction;
import com.musicslayer.cryptobuddy.util.Serialization;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

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
        TransactionPortfolioObj transactionPortfolioObj = new TransactionPortfolioObj(name);

        ArrayList<Transaction> transactionArrayList = Serialization.deserializeArrayList(o.getJSONArray("transactionArrayList").toStringOrNull(), Transaction.class);
        if(transactionArrayList != null) {
            for(Transaction transaction : transactionArrayList) {
                transactionPortfolioObj.addData(transaction);
            }
        }

        return transactionPortfolioObj;
    }
}
