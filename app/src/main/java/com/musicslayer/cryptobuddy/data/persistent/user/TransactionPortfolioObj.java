package com.musicslayer.cryptobuddy.data.persistent.user;

import com.musicslayer.cryptobuddy.data.bridge.DataBridge;
import com.musicslayer.cryptobuddy.transaction.Transaction;

import java.io.IOException;
import java.util.ArrayList;

public class TransactionPortfolioObj implements DataBridge.SerializableToJSON {
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

    @Override
    public void serializeToJSON(DataBridge.Writer o) throws IOException {
        o.beginObject()
                .serialize("!V!", "2", String.class)
                .serialize("name", name, String.class)
                .serializeArrayList("transactionArrayList", transactionArrayList, Transaction.class)
                .endObject();
    }

    public static TransactionPortfolioObj deserializeFromJSON(DataBridge.Reader o) throws IOException {
        o.beginObject();

        String version = o.deserialize("!V!", String.class);
        TransactionPortfolioObj transactionPortfolioObj;

        if("2".equals(version)) {
            String name = o.deserialize("name", String.class);
            ArrayList<Transaction> transactionArrayList = o.deserializeArrayList("transactionArrayList", Transaction.class);
            o.endObject();

            transactionPortfolioObj = new TransactionPortfolioObj(name);
            transactionPortfolioObj.transactionArrayList = transactionArrayList;
        }
        else {
            throw new IllegalStateException();
        }

        return transactionPortfolioObj;
    }
}
