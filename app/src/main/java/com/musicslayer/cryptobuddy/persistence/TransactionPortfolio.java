package com.musicslayer.cryptobuddy.persistence;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.HashMap;

import static android.content.Context.MODE_PRIVATE;

import com.musicslayer.cryptobuddy.util.ThrowableUtil;
import com.musicslayer.cryptobuddy.serialize.Serialization;

public class TransactionPortfolio {
    // This default will cause an error when deserialized. We should never see this value used.
    public final static String DEFAULT = "null";

    // Store the raw strings too in case we need them in a data dump.
    // Once everything has successfully loaded we stop updating these.
    public static HashMap<Integer, String> settings_transaction_portfolio_raw = new HashMap<>();

    public static ArrayList<TransactionPortfolioObj> settings_transaction_portfolio = new ArrayList<>();

    public static boolean isSaved(String name) {
        for(TransactionPortfolioObj p : settings_transaction_portfolio) {
            if(name.equals(p.name)) {
                return true;
            }
        }
        return false;
    }

    public static void saveAllData(Context context) {
        SharedPreferences settings = context.getSharedPreferences("transaction_portfolio_data", MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.clear();

        int size = settings_transaction_portfolio.size();
        editor.putInt("transaction_portfolio_size", size);

        for(int i = 0; i < size; i++) {
            TransactionPortfolioObj transactionPortfolioObj = settings_transaction_portfolio.get(i);
            editor.putString("transaction_portfolio" + i, Serialization.serialize(transactionPortfolioObj));
        }

        editor.apply();
    }

    public static void loadAllData(Context context) {
        settings_transaction_portfolio_raw = new HashMap<>();
        settings_transaction_portfolio = new ArrayList<>();

        SharedPreferences settings = context.getSharedPreferences("transaction_portfolio_data", MODE_PRIVATE);
        int size = settings.getInt("transaction_portfolio_size", 0);

        settings_transaction_portfolio_raw.put(-1, Integer.toString(size));

        for(int i = 0; i < size; i++) {
            String serialString = settings.getString("transaction_portfolio" + i, DEFAULT);
            settings_transaction_portfolio_raw.put(i, serialString == null ? "null" : serialString);

            TransactionPortfolioObj transactionPortfolioObj = Serialization.deserialize(serialString, TransactionPortfolioObj.class);
            settings_transaction_portfolio.add(transactionPortfolioObj);
        }
    }

    public static void addPortfolio(Context context, TransactionPortfolioObj transactionPortfolioObj) {
        settings_transaction_portfolio.add(transactionPortfolioObj);
        TransactionPortfolio.saveAllData(context);
    }

    public static void removePortfolio(Context context, TransactionPortfolioObj transactionPortfolioObj) {
        settings_transaction_portfolio.remove(transactionPortfolioObj);
        TransactionPortfolio.saveAllData(context);
    }

    public static void updatePortfolio(Context context, TransactionPortfolioObj transactionPortfolioObj) {
        SharedPreferences settings = context.getSharedPreferences("transaction_portfolio_data", MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        int idx = settings_transaction_portfolio.indexOf(transactionPortfolioObj);
        editor.putString("transaction_portfolio" + idx, Serialization.serialize(transactionPortfolioObj));

        editor.apply();
    }

    public static TransactionPortfolioObj getFromName(String name) {
        for(TransactionPortfolioObj p : settings_transaction_portfolio) {
            if(name.equals(p.name)) {
                return p;
            }
        }
        return null;
    }

    public static HashMap<String, String> getAllData() {
        HashMap<String, String> hashMap = new HashMap<>();

        for(int key : settings_transaction_portfolio_raw.keySet()) {
            if(key == -1) {
                hashMap.put("SIZE", settings_transaction_portfolio_raw.get(key));
            }
            else {
                hashMap.put("RAW" + key, settings_transaction_portfolio_raw.get(key));
            }
        }

        // We want the raw data even if this next piece errors.
        try {
            for(int i = 0; i < settings_transaction_portfolio.size(); i++) {
                TransactionPortfolioObj transactionPortfolioObj = settings_transaction_portfolio.get(i);
                hashMap.put("OBJ" + i, Serialization.serialize(transactionPortfolioObj));
            }
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
        }

        return hashMap;
    }

    public static void resetAllData(Context context) {
        settings_transaction_portfolio = new ArrayList<>();

        SharedPreferences settings = context.getSharedPreferences("transaction_portfolio_data", MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.clear();
        editor.apply();
    }
}
