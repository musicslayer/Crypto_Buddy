package com.musicslayer.cryptobuddy.persistence;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;

import com.musicslayer.cryptobuddy.app.App;
import com.musicslayer.cryptobuddy.data.Exportation;
import com.musicslayer.cryptobuddy.json.JSONWithNull;
import com.musicslayer.cryptobuddy.data.Serialization;
import com.musicslayer.cryptobuddy.util.SharedPreferencesUtil;

public class TransactionPortfolio implements Exportation.ExportableToJSON, Exportation.Versionable {
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

        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        String serialString = sharedPreferences.getString("transaction_portfolio" + idx, DEFAULT);
        return Serialization.deserialize(serialString, TransactionPortfolioObj.class);
    }

    public static void loadAllData(Context context) {
        // Only load portfolio names. Portfolios themselves are loaded when needed.
        settings_transaction_portfolio_names = new ArrayList<>();

        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        int size = sharedPreferences.getInt("transaction_portfolio_size", 0);

        for(int i = 0; i < size; i++) {
            String name;
            if(sharedPreferences.contains("transaction_portfolio_names" + i)) {
                name = sharedPreferences.getString("transaction_portfolio_names" + i, DEFAULT);
            }
            else {
                // Older installations won't have the name saved.
                // TODO To Remove!
                String serialString = sharedPreferences.getString("transaction_portfolio" + i, DEFAULT);
                TransactionPortfolioObj transactionPortfolioObj = Serialization.deserialize(serialString, TransactionPortfolioObj.class);
                name = transactionPortfolioObj.name;

                // Save the name now so this never has to be done again.
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("transaction_portfolio_names" + i, name);
                editor.apply();
            }
            settings_transaction_portfolio_names.add(name);
        }
    }

    public static void addPortfolio(Context context, TransactionPortfolioObj transactionPortfolioObj) {
        // Add this portfolio to the end and save data.
        settings_transaction_portfolio_names.add(transactionPortfolioObj.name);

        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        int size = settings_transaction_portfolio_names.size();
        editor.putInt("transaction_portfolio_size", size);
        editor.putString("transaction_portfolio" + (size - 1), Serialization.serialize(transactionPortfolioObj, TransactionPortfolioObj.class));
        editor.putString("transaction_portfolio_names" + (size - 1), transactionPortfolioObj.name);

        editor.apply();
    }

    public static void removePortfolio(Context context, String transactionPortfolioObjName) {
        // Remove this portfolio, and then shift others to condense.
        int idx = settings_transaction_portfolio_names.indexOf(transactionPortfolioObjName);
        settings_transaction_portfolio_names.remove(idx);

        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        int size = sharedPreferences.getInt("transaction_portfolio_size", 0);

        for(int i = idx; i < size - 1; i++) {
            String serialString = sharedPreferences.getString("transaction_portfolio" + (i + 1), DEFAULT);
            String name = sharedPreferences.getString("transaction_portfolio_names" + (i + 1), DEFAULT);

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
        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // We only need to update the portfolio object because the name can never change.
        int idx = settings_transaction_portfolio_names.indexOf(transactionPortfolioObj.name);
        editor.putString("transaction_portfolio" + idx, Serialization.serialize(transactionPortfolioObj, TransactionPortfolioObj.class));

        editor.apply();
    }

    public static void resetAllData(Context context) {
        settings_transaction_portfolio_names = new ArrayList<>();

        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.clear();
        editor.apply();
    }

    public static boolean canExport() {
        return true;
    }

    public String exportationVersion() {
        return "1";
    }

    public static String exportDataToJSON() throws org.json.JSONException {
        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());

        JSONWithNull.JSONObjectWithNull o = new JSONWithNull.JSONObjectWithNull();

        String sizeKey = "transaction_portfolio_size";
        int size = sharedPreferences.getInt(sizeKey, 0);
        o.put(sizeKey, size, Integer.class);

        for(int i = 0; i < size; i++) {
            String nameKey = "transaction_portfolio_names" + i;
            String serialNameString = sharedPreferences.getString(nameKey, DEFAULT);
            o.put(nameKey, serialNameString, String.class);

            String key = "transaction_portfolio" + i;
            String serialString = sharedPreferences.getString(key, DEFAULT);
            o.put(key, serialString, String.class);
        }

        return o.toStringOrNull();
    }


    public static void importDataFromJSON(String s, String version) throws org.json.JSONException {
        JSONWithNull.JSONObjectWithNull o = new JSONWithNull.JSONObjectWithNull(s);

        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        String sizeKey = "transaction_portfolio_size";
        int size = o.get(sizeKey, Integer.class);
        editor.putInt(sizeKey, size);

        for(int i = 0; i < size; i++) {
            String nameKey = "transaction_portfolio_names" + i;
            String nameValue = o.get(nameKey, String.class);
            editor.putString(nameKey, nameValue);

            String key = "transaction_portfolio" + i;
            String value = o.get(key, String.class);
            editor.putString(key, Serialization.validate(value, TransactionPortfolioObj.class));
        }

        editor.apply();

        // Reinitialize data.
        TransactionPortfolio.loadAllData(App.applicationContext);
    }
}
