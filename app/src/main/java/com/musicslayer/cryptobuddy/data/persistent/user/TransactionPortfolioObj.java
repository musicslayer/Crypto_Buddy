package com.musicslayer.cryptobuddy.data.persistent.user;

import com.musicslayer.cryptobuddy.data.bridge.DataBridge;
import com.musicslayer.cryptobuddy.data.bridge.LegacyDataBridge;
import com.musicslayer.cryptobuddy.data.bridge.LegacySerialization;
import com.musicslayer.cryptobuddy.transaction.Transaction;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

public class TransactionPortfolioObj implements LegacySerialization.SerializableToJSON, LegacySerialization.Versionable, DataBridge.SerializableToJSON {
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

    public static String legacy_serializationVersion() {
        return "1";
    }

    public static String legacy_serializationType(String version) {
        return "!OBJECT!";
    }

    @Override
    public String legacy_serializeToJSON() throws JSONException {
        return new LegacyDataBridge.JSONObjectDataBridge()
            .serialize("name", name, String.class)
            .serializeArrayList("transactionArrayList", transactionArrayList, Transaction.class)
            .toStringOrNull();
    }

    public static TransactionPortfolioObj legacy_deserializeFromJSON(String s, String version) throws JSONException {
        LegacyDataBridge.JSONObjectDataBridge o = new LegacyDataBridge.JSONObjectDataBridge(s);
        String name = o.deserialize("name", String.class);
        ArrayList<Transaction> transactionArrayList = o.deserializeArrayList("transactionArrayList", Transaction.class);

        TransactionPortfolioObj transactionPortfolioObj = new TransactionPortfolioObj(name);
        transactionPortfolioObj.transactionArrayList = transactionArrayList;

        return transactionPortfolioObj;
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
