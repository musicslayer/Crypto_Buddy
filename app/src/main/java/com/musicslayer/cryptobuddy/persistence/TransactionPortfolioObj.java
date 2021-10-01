package com.musicslayer.cryptobuddy.persistence;

import com.musicslayer.cryptobuddy.transaction.Transaction;

import org.json.JSONObject;

import java.util.ArrayList;

public class TransactionPortfolioObj {
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

    public String serialize() {
        return "{\"name\":\"" + name + "\",\"transactionArrayList\":" + Transaction.serializeArray(transactionArrayList) + "}";
    }

    public static TransactionPortfolioObj deserialize(String s) {
        try {
            JSONObject o = new JSONObject(s);
            String name = o.getString("name");
            TransactionPortfolioObj transactionPortfolioObj = new TransactionPortfolioObj(name);

            ArrayList<Transaction> transactionArrayList = Transaction.deserializeArray(o.getJSONArray("transactionArrayList").toString());
            for(Transaction transaction : transactionArrayList) {
                transactionPortfolioObj.addData(transaction);
            }

            return transactionPortfolioObj;
        }
        catch(Exception e) {
            return null;
        }
    }
}
