package com.musicslayer.cryptobuddy.persistence;

import com.musicslayer.cryptobuddy.data.DataBridge;
import com.musicslayer.cryptobuddy.data.Serialization;
import com.musicslayer.cryptobuddy.transaction.Transaction;

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

    public static String serializationVersion() {
        return "1";
    }

    public static String serializationType(String version) {
        return "!OBJECT!";
    }

    @Override
    public String serializeToJSON() throws org.json.JSONException {
        return new DataBridge.JSONObjectDataBridge()
            .serialize("name", name, String.class)
            .serializeArrayList("transactionArrayList", transactionArrayList, Transaction.class)
            .toStringOrNull();
    }

    public static TransactionPortfolioObj deserializeFromJSON(String s, String version) throws org.json.JSONException {
        DataBridge.JSONObjectDataBridge o = new DataBridge.JSONObjectDataBridge(s);
        String name = o.deserialize("name", String.class);
        ArrayList<Transaction> transactionArrayList = o.deserializeArrayList("transactionArrayList", Transaction.class);

        TransactionPortfolioObj transactionPortfolioObj = new TransactionPortfolioObj(name);
        transactionPortfolioObj.transactionArrayList = transactionArrayList;

        return transactionPortfolioObj;
    }
}
