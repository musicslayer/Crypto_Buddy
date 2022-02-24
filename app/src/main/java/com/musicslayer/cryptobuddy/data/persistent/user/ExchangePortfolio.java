package com.musicslayer.cryptobuddy.data.persistent.user;

import android.content.SharedPreferences;

import com.musicslayer.cryptobuddy.data.bridge.DataBridge;
import com.musicslayer.cryptobuddy.util.HashMapUtil;
import com.musicslayer.cryptobuddy.util.SharedPreferencesUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class ExchangePortfolio extends PersistentUserDataStore implements DataBridge.ExportableToJSON {
    public String getName() { return "ExchangePortfolio"; }

    public boolean isVisible() { return true; }
    public String doExport() { return DataBridge.exportData(this, ExchangePortfolio.class); }
    public void doImport(String s) { DataBridge.importData(this, s, ExchangePortfolio.class); }

    // This default will cause an error when deserialized. We should never see this value used.
    public final static String DEFAULT = "null";

    public static ArrayList<String> settings_exchange_portfolio_names = new ArrayList<>();

    public String getSharedPreferencesKey() {
        return "exchange_portfolio_data";
    }

    public static boolean isSaved(String name) {
        return settings_exchange_portfolio_names.contains(name);
    }

    public ExchangePortfolioObj getFromName(String name) {
        if(!isSaved(name)) {
            return null;
        }

        int idx = settings_exchange_portfolio_names.indexOf(name);

        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        String serialString = sharedPreferences.getString("exchange_portfolio" + idx, DEFAULT);
        return DataBridge.deserialize(serialString, ExchangePortfolioObj.class);
    }

    public void addPortfolio(ExchangePortfolioObj exchangePortfolioObj) {
        // Add this portfolio to the end and save data.
        settings_exchange_portfolio_names.add(exchangePortfolioObj.name);

        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        int size = settings_exchange_portfolio_names.size();
        editor.putInt("exchange_portfolio_size", size);
        editor.putString("exchange_portfolio" + (size - 1), DataBridge.serialize(exchangePortfolioObj, ExchangePortfolioObj.class));
        editor.putString("exchange_portfolio_names" + (size - 1), exchangePortfolioObj.name);

        editor.apply();
    }

    public void removePortfolio(String exchangePortfolioObjName) {
        // Remove this portfolio, and then shift others to condense.
        int idx = settings_exchange_portfolio_names.indexOf(exchangePortfolioObjName);
        settings_exchange_portfolio_names.remove(idx);

        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        int size = sharedPreferences.getInt("exchange_portfolio_size", 0);

        for(int i = idx; i < size - 1; i++) {
            String serialString = sharedPreferences.getString("exchange_portfolio" + (i + 1), DEFAULT);
            String name = sharedPreferences.getString("exchange_portfolio_names" + (i + 1), DEFAULT);

            editor.putString("exchange_portfolio" + i, serialString);
            editor.putString("exchange_portfolio_names" + i, name);
        }

        // Delete last element.
        editor.remove("exchange_portfolio_names" + (size - 1));
        editor.remove("exchange_portfolio" + (size - 1));

        // Update size.
        editor.putInt("exchange_portfolio_size", size - 1);

        editor.apply();
    }

    public void renamePortfolio(String exchangePortfolioObjOldName, String exchangePortfolioObjNewName) {
        ExchangePortfolioObj exchangePortfolioObj = getFromName(exchangePortfolioObjOldName);
        exchangePortfolioObj.name = exchangePortfolioObjNewName;

        int idx = settings_exchange_portfolio_names.indexOf(exchangePortfolioObjOldName);
        settings_exchange_portfolio_names.set(idx, exchangePortfolioObjNewName);

        updatePortfolio(exchangePortfolioObj);
    }

    public void updatePortfolio(ExchangePortfolioObj exchangePortfolioObj) {
        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        int idx = settings_exchange_portfolio_names.indexOf(exchangePortfolioObj.name);
        editor.putString("exchange_portfolio_names" + idx, exchangePortfolioObj.name);
        editor.putString("exchange_portfolio" + idx, DataBridge.serialize(exchangePortfolioObj, ExchangePortfolioObj.class));

        editor.apply();
    }

    public void saveAllData() {
        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.clear();

        int size = settings_exchange_portfolio_names.size();
        editor.putInt("exchange_portfolio_size", size);

        for(int i = 0; i < size; i++) {
            String name = settings_exchange_portfolio_names.get(i);
            editor.putString("exchange_portfolio_names" + i, name);

            // Portfolios have to be loaded and then saved again.
            String serialString = sharedPreferences.getString("exchange_portfolio" + i, DEFAULT);
            editor.putString("exchange_portfolio" + i, DataBridge.cycleSerialization(serialString, ExchangePortfolioObj.class));
        }

        editor.apply();
    }

    public void loadAllData() {
        // Only load portfolio names. Portfolios themselves are loaded when needed.
        settings_exchange_portfolio_names = new ArrayList<>();

        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        int size = sharedPreferences.getInt("exchange_portfolio_size", 0);

        for(int i = 0; i < size; i++) {
            String name;
            if(sharedPreferences.contains("exchange_portfolio_names" + i)) {
                name = sharedPreferences.getString("exchange_portfolio_names" + i, DEFAULT);
            }
            else {
                // Older installations won't have the name saved.
                // REMOVE
                String serialString = sharedPreferences.getString("exchange_portfolio" + i, DEFAULT);
                ExchangePortfolioObj exchangePortfolioObj = DataBridge.deserialize(serialString, ExchangePortfolioObj.class);
                name = exchangePortfolioObj.name;

                // Save the name now so this never has to be done again.
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("exchange_portfolio_names" + i, name);
                editor.apply();
            }
            settings_exchange_portfolio_names.add(name);
        }
    }

    public void resetAllData() {
        settings_exchange_portfolio_names = new ArrayList<>();

        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.clear();
        editor.apply();
    }

    @Override
    public void exportDataToJSON(DataBridge.Writer o) throws IOException {
        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());

        o.beginObject();
        o.serialize("!V!", "1", String.class);

        String sizeKey = "exchange_portfolio_size";
        int size = sharedPreferences.getInt(sizeKey, 0);
        o.serialize(sizeKey, size, Integer.class);

        for(int i = 0; i < size; i++) {
            String nameKey = "exchange_portfolio_names" + i;
            String serialNameString = sharedPreferences.getString(nameKey, DEFAULT);
            o.serialize(nameKey, serialNameString, String.class);

            String key = "exchange_portfolio" + i;
            String serialString = sharedPreferences.getString(key, DEFAULT);
            o.serialize(key, serialString, String.class);
        }

        o.endObject();
    }


    @Override
    public void importDataFromJSON(DataBridge.Reader o) throws IOException {
        o.beginObject();

        String version = o.deserialize("!V!", String.class);
        if(!"1".equals(version)) {
            throw new IllegalStateException();
        }

        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Create HashMap of all existing portfolios.
        HashMap<String, String> hashMap = new HashMap<>();
        for(int i = 0; i < settings_exchange_portfolio_names.size(); i++) {
            String name = sharedPreferences.getString("exchange_portfolio_names" + i, DEFAULT);
            String value = sharedPreferences.getString("exchange_portfolio" + i, DEFAULT);
            HashMapUtil.putValueInMap(hashMap, name, value);
        }

        // Merge in the imported portfolios.
        int size = o.deserialize("exchange_portfolio_size", Integer.class);
        for(int i = 0; i < size; i++) {
            String name = o.deserialize("exchange_portfolio_names" + i, String.class);
            String value = o.deserialize("exchange_portfolio" + i, String.class);
            HashMapUtil.putValueInMap(hashMap, name, DataBridge.cycleSerialization(value, ExchangePortfolioObj.class));
        }

        // Erase portfolios, rewrite, and reload.
        editor.clear();

        ArrayList<String> keySet = new ArrayList<>(hashMap.keySet());

        editor.putInt("exchange_portfolio_size", keySet.size());
        for(int i = 0; i < keySet.size(); i++) {
            String nameKey = "exchange_portfolio_names" + i;
            String nameValue = keySet.get(i);
            editor.putString(nameKey, nameValue);

            String key = "exchange_portfolio" + i;
            String value = HashMapUtil.getValueFromMap(hashMap, keySet.get(i));
            editor.putString(key, value);
        }

        editor.apply();

        o.endObject();

        // Reinitialize data.
        loadAllData();
    }
}
