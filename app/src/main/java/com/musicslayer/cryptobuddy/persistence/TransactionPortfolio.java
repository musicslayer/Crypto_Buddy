package com.musicslayer.cryptobuddy.persistence;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

import com.musicslayer.cryptobuddy.util.Serialization;

public class TransactionPortfolio {
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
        settings_transaction_portfolio = new ArrayList<>();

        SharedPreferences settings = context.getSharedPreferences("transaction_portfolio_data", MODE_PRIVATE);
        int size = settings.getInt("transaction_portfolio_size", 0);

        for(int i = 0; i < size; i++) {
            String serialString = settings.getString("transaction_portfolio" + i, "");
            TransactionPortfolioObj transactionPortfolioObj = Serialization.deserialize(serialString, TransactionPortfolioObj.class);

            // If there is any problem at all, don't add this one.
            if(transactionPortfolioObj != null) {
                settings_transaction_portfolio.add(transactionPortfolioObj);
            }
        }

        // Data might have changed if transactions removed cryptos that no longer exist.
        saveAllData(context);
    }

    public static void addPortfolio(Context context, TransactionPortfolioObj transactionPortfolioObj) {
        settings_transaction_portfolio.add(transactionPortfolioObj);

        TransactionPortfolio.saveAllData(context);
    }

    public static void removePortfolio(Context context, TransactionPortfolioObj transactionPortfolioObj) {
        settings_transaction_portfolio.remove(transactionPortfolioObj);

        TransactionPortfolio.saveAllData(context);
    }

    public static TransactionPortfolioObj getFromName(String name) {
        for(TransactionPortfolioObj p : settings_transaction_portfolio) {
            if(name.equals(p.name)) {
                return p;
            }
        }
        return null;
    }

    public static void resetAllData(Context context) {
        settings_transaction_portfolio = new ArrayList<>();

        SharedPreferences settings = context.getSharedPreferences("transaction_portfolio_data", MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.clear();
        editor.putInt("transaction_portfolio_size", 0);
        editor.apply();
    }
}
