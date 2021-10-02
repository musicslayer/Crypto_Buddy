package com.musicslayer.cryptobuddy.persistence;

import com.musicslayer.cryptobuddy.transaction.Transaction;
import com.musicslayer.cryptobuddy.util.Serialization;

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

    public String serializeToJSON() {
        return "{\"name\":\"" + name + "\",\"transactionArrayList\":" + Serialization.serializeArrayList(transactionArrayList) + "}";
    }

    public static TransactionPortfolioObj deserializeFromJSON(String s) throws org.json.JSONException {
        JSONObject o = new JSONObject(s);
        String name = o.getString("name");
        TransactionPortfolioObj transactionPortfolioObj = new TransactionPortfolioObj(name);

        ArrayList<Transaction> transactionArrayList = Serialization.deserializeArrayList(o.getJSONArray("transactionArrayList").toString(), Transaction.class);
        if(transactionArrayList != null) {
            for(Transaction transaction : transactionArrayList) {
                transactionPortfolioObj.addData(transaction);
            }
        }

        return transactionPortfolioObj;
    }
}
