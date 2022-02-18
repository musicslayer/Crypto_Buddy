package com.musicslayer.cryptobuddy.persistence;

import com.musicslayer.cryptobuddy.json.JSONWithNull;
import com.musicslayer.cryptobuddy.transaction.Transaction;
import com.musicslayer.cryptobuddy.data.Serialization;

import java.util.ArrayList;

public class TransactionPortfolioObj implements Serialization.SerializableToJSON, Serialization.Versionable {
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
        return new JSONWithNull.JSONObjectWithNull()
            .put("name", Serialization.string_serialize(name))
            .put("transactionArrayList", new JSONWithNull.JSONArrayWithNull(Serialization.serializeArrayList(transactionArrayList)))
            .toStringOrNull();
    }

    public static TransactionPortfolioObj deserializeFromJSON1(String s) throws org.json.JSONException {
        JSONWithNull.JSONObjectWithNull o = new JSONWithNull.JSONObjectWithNull(s);
        String name = Serialization.string_deserialize(o.getString("name"));
        ArrayList<Transaction> transactionArrayList = Serialization.deserializeArrayList(o.getJSONArrayString("transactionArrayList"), Transaction.class);

        TransactionPortfolioObj transactionPortfolioObj = new TransactionPortfolioObj(name);
        transactionPortfolioObj.transactionArrayList = transactionArrayList;

        return transactionPortfolioObj;
    }
}
