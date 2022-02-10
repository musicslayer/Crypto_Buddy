package com.musicslayer.cryptobuddy.persistence;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.HashMap;

import static android.content.Context.MODE_PRIVATE;

import com.musicslayer.cryptobuddy.serialize.Serialization;

public class TransactionPortfolio {
    // This default will cause an error when deserialized. We should never see this value used.
    public final static String DEFAULT = "null";

    public static ArrayList<String> settings_transaction_portfolio_names = new ArrayList<>();

    public static boolean isSaved(String name) {
        return settings_transaction_portfolio_names.contains(name);
    }

    public static TransactionPortfolioObj getFromName(Context context, String name) {
        if(!isSaved(name)) {
            return null;
        }

        int idx = settings_transaction_portfolio_names.indexOf(name);

        SharedPreferences settings = context.getSharedPreferences("transaction_portfolio_data", MODE_PRIVATE);
        String serialString = settings.getString("transaction_portfolio" + idx, DEFAULT);
        return Serialization.deserialize(serialString, TransactionPortfolioObj.class);
    }

    public static void loadAllData(Context context) {
        // Only load portfolio names. Portfolios themselves are loaded when needed.
        // TODO People who used the app before won't have names saved.
        settings_transaction_portfolio_names = new ArrayList<>();

        SharedPreferences settings = context.getSharedPreferences("transaction_portfolio_data", MODE_PRIVATE);
        int size = settings.getInt("transaction_portfolio_size", 0);

        for(int i = 0; i < size; i++) {
            String name = settings.getString("transaction_portfolio_names" + i, DEFAULT);
            settings_transaction_portfolio_names.add(name);
        }
    }

    public static void addPortfolio(Context context, TransactionPortfolioObj transactionPortfolioObj) {
        // Add this portfolio to the end and save data.
        settings_transaction_portfolio_names.add(transactionPortfolioObj.name);

        SharedPreferences settings = context.getSharedPreferences("transaction_portfolio_data", MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        int size = settings_transaction_portfolio_names.size();
        editor.putInt("transaction_portfolio_size", size);
        editor.putString("transaction_portfolio" + (size - 1), Serialization.serialize(transactionPortfolioObj));
        editor.putString("transaction_portfolio_names" + (size - 1), transactionPortfolioObj.name);

        editor.apply();
    }

    // TODO Can we just pass in the name?
    public static void removePortfolio(Context context, TransactionPortfolioObj transactionPortfolioObj) {
        // Remove this portfolio, and then shift others to condense.
        int idx = settings_transaction_portfolio_names.indexOf(transactionPortfolioObj.name);
        settings_transaction_portfolio_names.remove(idx);

        SharedPreferences settings = context.getSharedPreferences("transaction_portfolio_data", MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        int size = settings.getInt("transaction_portfolio_size", 0);

        for(int i = idx; i < size - 1; i++) {
            String serialString = settings.getString("transaction_portfolio" + (i + 1), DEFAULT);
            String name = settings.getString("transaction_portfolio_names" + (i + 1), DEFAULT);

            editor.putString("transaction_portfolio" + i, serialString);
            editor.putString("transaction_portfolio_names" + i, name);
        }

        // Delete last element.
        editor.remove("transaction_portfolio_names" + (size - 1));
        editor.remove("transaction_portfolio" + (size - 1));

        // Update size.
        editor.putInt("transaction_portfolio_size", size - 1);

        editor.apply();
    }

    public static void updatePortfolio(Context context, TransactionPortfolioObj transactionPortfolioObj) {
        SharedPreferences settings = context.getSharedPreferences("transaction_portfolio_data", MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        // We only need to update the portfolio object because the name can never change.
        int idx = settings_transaction_portfolio_names.indexOf(transactionPortfolioObj.name);
        editor.putString("transaction_portfolio" + idx, Serialization.serialize(transactionPortfolioObj));

        editor.apply();
    }

    public static HashMap<String, String> getAllData() {
        HashMap<String, String> hashMap = new HashMap<>();

        // TODO
        /*
        for(int key : settings_transaction_portfolio_raw.keySet()) {
            if(key == -1) {
                hashMap.put("SIZE", settings_transaction_portfolio_raw.get(key));
            }
            else {
                hashMap.put("RAW" + key, settings_transaction_portfolio_raw.get(key));
            }
        }

        hashMap.put("SIZE", settings_transaction_portfolio_raw.get(key));

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

         */

        return hashMap;
    }

    public static void resetAllData(Context context) {
        settings_transaction_portfolio_names = new ArrayList<>();

        SharedPreferences settings = context.getSharedPreferences("transaction_portfolio_data", MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.clear();
        editor.apply();
    }
}
