package com.musicslayer.cryptobuddy.data.persistent.user;

import android.content.SharedPreferences;

import java.util.ArrayList;

import com.musicslayer.cryptobuddy.data.bridge.LegacyDataBridge;
import com.musicslayer.cryptobuddy.data.bridge.Exportation;
import com.musicslayer.cryptobuddy.data.bridge.Serialization;
import com.musicslayer.cryptobuddy.util.SharedPreferencesUtil;

public class TransactionPortfolio extends PersistentUserDataStore implements Exportation.ExportableToJSON, Exportation.Versionable {
    public String getName() { return "TransactionPortfolio"; }

    public boolean canExport() { return true; }
    public String doExport() { return Exportation.exportData(this, TransactionPortfolio.class); }
    public void doImport(String s) { Exportation.importData(this, s, TransactionPortfolio.class); }

    // This default will cause an error when deserialized. We should never see this value used.
    public final static String DEFAULT = "null";

    public static ArrayList<String> settings_transaction_portfolio_names = new ArrayList<>();

    public String getSharedPreferencesKey() {
        return "transaction_portfolio_data";
    }

    public static boolean isSaved(String name) {
        return settings_transaction_portfolio_names.contains(name);
    }

    public TransactionPortfolioObj getFromName(String name) {
        if(!isSaved(name)) {
            return null;
        }

        int idx = settings_transaction_portfolio_names.indexOf(name);

        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        String serialString = sharedPreferences.getString("transaction_portfolio" + idx, DEFAULT);
        return Serialization.deserialize(serialString, TransactionPortfolioObj.class);
    }

    public void addPortfolio(TransactionPortfolioObj transactionPortfolioObj) {
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

    public void removePortfolio(String transactionPortfolioObjName) {
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

    public void updatePortfolio(TransactionPortfolioObj transactionPortfolioObj) {
        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // We only need to update the portfolio object because the name can never change.
        int idx = settings_transaction_portfolio_names.indexOf(transactionPortfolioObj.name);
        editor.putString("transaction_portfolio" + idx, Serialization.serialize(transactionPortfolioObj, TransactionPortfolioObj.class));

        editor.apply();
    }

    public void saveAllData() {
        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.clear();

        int size = settings_transaction_portfolio_names.size();
        editor.putInt("transaction_portfolio_size", size);

        for(int i = 0; i < size; i++) {
            String name = settings_transaction_portfolio_names.get(i);
            editor.putString("transaction_portfolio_names" + i, name);

            // Portfolios have to be loaded and then saved again.
            String serialString = sharedPreferences.getString("transaction_portfolio" + i, DEFAULT);
            editor.putString("transaction_portfolio" + i, Serialization.cycle(serialString, TransactionPortfolio.class));
        }

        editor.apply();
    }

    public void loadAllData() {
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

    public void resetAllData() {
        settings_transaction_portfolio_names = new ArrayList<>();

        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.clear();
        editor.apply();
    }

    public static String exportationVersion() {
        return "1";
    }

    public static String exportationType(String version) {
        return "!OBJECT!";
    }

    public String exportDataToJSON() throws org.json.JSONException {
        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());

        LegacyDataBridge.JSONObjectDataBridge o = new LegacyDataBridge.JSONObjectDataBridge();

        String sizeKey = "transaction_portfolio_size";
        int size = sharedPreferences.getInt(sizeKey, 0);
        o.serialize(sizeKey, size, Integer.class);

        for(int i = 0; i < size; i++) {
            String nameKey = "transaction_portfolio_names" + i;
            String serialNameString = sharedPreferences.getString(nameKey, DEFAULT);
            o.serialize(nameKey, serialNameString, String.class);

            String key = "transaction_portfolio" + i;
            String serialString = sharedPreferences.getString(key, DEFAULT);
            o.serialize(key, serialString, String.class);
        }

        return o.toStringOrNull();
    }

    public void importDataFromJSON(String s, String version) throws org.json.JSONException {
        LegacyDataBridge.JSONObjectDataBridge o = new LegacyDataBridge.JSONObjectDataBridge(s);

        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        String sizeKey = "transaction_portfolio_size";
        int size = o.deserialize(sizeKey, Integer.class);
        editor.putInt(sizeKey, size);

        for(int i = 0; i < size; i++) {
            String nameKey = "transaction_portfolio_names" + i;
            String nameValue = o.deserialize(nameKey, String.class);
            editor.putString(nameKey, nameValue);

            String key = "transaction_portfolio" + i;
            String value = o.deserialize(key, String.class);
            editor.putString(key, Serialization.cycle(value, TransactionPortfolioObj.class));
        }

        editor.apply();

        // Reinitialize data.
        loadAllData();
    }
}
