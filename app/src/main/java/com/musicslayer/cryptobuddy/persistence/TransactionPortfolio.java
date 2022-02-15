package com.musicslayer.cryptobuddy.persistence;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

import com.musicslayer.cryptobuddy.serialize.Serialization;

public class TransactionPortfolio {
    // This default will cause an error when deserialized. We should never see this value used.
    public final static String DEFAULT = "null";

    public static ArrayList<String> settings_transaction_portfolio_names = new ArrayList<>();

    public static String getSharedPreferencesKey() {
        return "transaction_portfolio_data";
    }

    public static boolean isSaved(String name) {
        return settings_transaction_portfolio_names.contains(name);
    }

    public static TransactionPortfolioObj getFromName(Context context, String name) {
        if(!isSaved(name)) {
            return null;
        }

        int idx = settings_transaction_portfolio_names.indexOf(name);

        SharedPreferences settings = context.getSharedPreferences(getSharedPreferencesKey(), MODE_PRIVATE);
        String serialString = settings.getString("transaction_portfolio" + idx, DEFAULT);
        return Serialization.deserialize(serialString, TransactionPortfolioObj.class);
    }

    public static void loadAllData(Context context) {
        // Only load portfolio names. Portfolios themselves are loaded when needed.
        settings_transaction_portfolio_names = new ArrayList<>();

        SharedPreferences settings = context.getSharedPreferences(getSharedPreferencesKey(), MODE_PRIVATE);
        int size = settings.getInt("transaction_portfolio_size", 0);

        for(int i = 0; i < size; i++) {
            String name;
            if(settings.contains("transaction_portfolio_names" + i)) {
                name = settings.getString("transaction_portfolio_names" + i, DEFAULT);
            }
            else {
                // Older installations won't have the name saved.
                // TODO To Remove!
                String serialString = settings.getString("transaction_portfolio" + i, DEFAULT);
                TransactionPortfolioObj transactionPortfolioObj = Serialization.deserialize(serialString, TransactionPortfolioObj.class);
                name = transactionPortfolioObj.name;

                // Save the name now so this never has to be done again.
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("transaction_portfolio_names" + i, name);
                editor.apply();
            }
            settings_transaction_portfolio_names.add(name);
        }
    }

    public static void addPortfolio(Context context, TransactionPortfolioObj transactionPortfolioObj) {
        // Add this portfolio to the end and save data.
        settings_transaction_portfolio_names.add(transactionPortfolioObj.name);

        SharedPreferences settings = context.getSharedPreferences(getSharedPreferencesKey(), MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        int size = settings_transaction_portfolio_names.size();
        editor.putInt("transaction_portfolio_size", size);
        editor.putString("transaction_portfolio" + (size - 1), Serialization.serialize(transactionPortfolioObj));
        editor.putString("transaction_portfolio_names" + (size - 1), transactionPortfolioObj.name);

        editor.apply();
    }

    public static void removePortfolio(Context context, String transactionPortfolioObjName) {
        // Remove this portfolio, and then shift others to condense.
        int idx = settings_transaction_portfolio_names.indexOf(transactionPortfolioObjName);
        settings_transaction_portfolio_names.remove(idx);

        SharedPreferences settings = context.getSharedPreferences(getSharedPreferencesKey(), MODE_PRIVATE);
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
        SharedPreferences settings = context.getSharedPreferences(getSharedPreferencesKey(), MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        // We only need to update the portfolio object because the name can never change.
        int idx = settings_transaction_portfolio_names.indexOf(transactionPortfolioObj.name);
        editor.putString("transaction_portfolio" + idx, Serialization.serialize(transactionPortfolioObj));

        editor.apply();
    }

    public static void resetAllData(Context context) {
        settings_transaction_portfolio_names = new ArrayList<>();

        SharedPreferences settings = context.getSharedPreferences(getSharedPreferencesKey(), MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.clear();
        editor.apply();
    }

    //public String exportVersion() { return "1"; }

    public static boolean canExport() { return true; }

    public static String exportToJSON(Context context) throws org.json.JSONException {
        SharedPreferences settings = context.getSharedPreferences(getSharedPreferencesKey(), MODE_PRIVATE);

        Serialization.JSONObjectWithNull o = new Serialization.JSONObjectWithNull();

        String sizeKey = "transaction_portfolio_size";
        int size = settings.getInt(sizeKey, 0);
        o.put(sizeKey, Serialization.int_serialize(size));

        for(int i = 0; i < size; i++) {
            String nameKey = "transaction_portfolio_names" + i;
            String serialNameString = settings.getString(nameKey, DEFAULT);
            o.put(nameKey, serialNameString);

            String key = "transaction_portfolio" + i;
            String serialString = settings.getString(key, DEFAULT);
            o.put(key, serialString);
        }

        return o.toStringOrNull();
    }


    public static void importFromJSON1(Context context, String s) throws org.json.JSONException {
        Serialization.JSONObjectWithNull o = new Serialization.JSONObjectWithNull(s);

        SharedPreferences settings = context.getSharedPreferences(getSharedPreferencesKey(), MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        String sizeKey = "transaction_portfolio_size";
        int size = Serialization.int_deserialize(o.getString(sizeKey));
        editor.putInt(sizeKey, size);

        for(int i = 0; i < size; i++) {
            String nameKey = "transaction_portfolio_names" + i;
            String nameValue = o.getString(nameKey);

            editor.putString(nameKey, nameValue);

            String key = "transaction_portfolio" + i;
            String value = o.getString(key);

            // This round trip of deserializing and serializing ensures that the data is valid.
            TransactionPortfolioObj dummyTransactionPortfolioObj = Serialization.deserialize(value, TransactionPortfolioObj.class);
            editor.putString(key, Serialization.serialize(dummyTransactionPortfolioObj));
        }

        editor.apply();

        // Reinitialize data.
        TransactionPortfolio.loadAllData(context);
    }
}
