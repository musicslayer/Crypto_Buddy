package com.musicslayer.cryptobuddy.data.persistent.user;

import android.content.SharedPreferences;

import java.io.IOException;
import java.util.ArrayList;

import com.musicslayer.cryptobuddy.data.bridge.DataBridge;
import com.musicslayer.cryptobuddy.util.SharedPreferencesUtil;

public class AddressPortfolio extends PersistentUserDataStore implements DataBridge.ExportableToJSON {
    public String getName() { return "AddressPortfolio"; }

    public boolean canExport() { return true; }
    public String doExport() { return DataBridge.exportData(this, AddressPortfolio.class); }
    public void doImport(String s) { DataBridge.importData(this, s, AddressPortfolio.class); }

    // This default will cause an error when deserialized. We should never see this value used.
    public final static String DEFAULT = "null";

    public static ArrayList<String> settings_address_portfolio_names = new ArrayList<>();

    public String getSharedPreferencesKey() {
        return "address_portfolio_data";
    }

    public static boolean isSaved(String name) {
        return settings_address_portfolio_names.contains(name);
    }

    public AddressPortfolioObj getFromName(String name) {
        if(!isSaved(name)) {
            return null;
        }

        int idx = settings_address_portfolio_names.indexOf(name);

        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        String serialString = sharedPreferences.getString("address_portfolio" + idx, DEFAULT);
        return DataBridge.deserialize(serialString, AddressPortfolioObj.class);
    }

    public void addPortfolio(AddressPortfolioObj addressPortfolioObj) {
        // Add this portfolio to the end and save data.
        settings_address_portfolio_names.add(addressPortfolioObj.name);

        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        int size = settings_address_portfolio_names.size();
        editor.putInt("address_portfolio_size", size);
        editor.putString("address_portfolio" + (size - 1), DataBridge.serialize(addressPortfolioObj, AddressPortfolioObj.class));
        editor.putString("address_portfolio_names" + (size - 1), addressPortfolioObj.name);

        editor.apply();
    }

    public void removePortfolio(String addressPortfolioObjName) {
        // Remove this portfolio, and then shift others to condense.
        int idx = settings_address_portfolio_names.indexOf(addressPortfolioObjName);
        settings_address_portfolio_names.remove(idx);

        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        int size = sharedPreferences.getInt("address_portfolio_size", 0);

        for(int i = idx; i < size - 1; i++) {
            String serialString = sharedPreferences.getString("address_portfolio" + (i + 1), DEFAULT);
            String name = sharedPreferences.getString("address_portfolio_names" + (i + 1), DEFAULT);

            editor.putString("address_portfolio" + i, serialString);
            editor.putString("address_portfolio_names" + i, name);
        }

        // Delete last element.
        editor.remove("address_portfolio_names" + (size - 1));
        editor.remove("address_portfolio" + (size - 1));

        // Update size.
        editor.putInt("address_portfolio_size", size - 1);

        editor.apply();
    }

    public void updatePortfolio(AddressPortfolioObj addressPortfolioObj) {
        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // We only need to update the portfolio object because the name can never change.
        int idx = settings_address_portfolio_names.indexOf(addressPortfolioObj.name);
        editor.putString("address_portfolio" + idx, DataBridge.serialize(addressPortfolioObj, AddressPortfolioObj.class));

        editor.apply();
    }

    public void saveAllData() {
        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.clear();

        int size = settings_address_portfolio_names.size();
        editor.putInt("address_portfolio_size", size);

        for(int i = 0; i < size; i++) {
            String name = settings_address_portfolio_names.get(i);
            editor.putString("address_portfolio_names" + i, name);

            // Portfolios have to be loaded and then saved again.
            String serialString = sharedPreferences.getString("address_portfolio" + i, DEFAULT);
            editor.putString("address_portfolio" + i, DataBridge.cycleSerialization(serialString, AddressPortfolioObj.class));
        }

        editor.apply();
    }

    public void loadAllData() {
        // Only load portfolio names. Portfolios themselves are loaded when needed.
        settings_address_portfolio_names = new ArrayList<>();

        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        int size = sharedPreferences.getInt("address_portfolio_size", 0);

        for(int i = 0; i < size; i++) {
            String name;
            if(sharedPreferences.contains("address_portfolio_names" + i)) {
                name = sharedPreferences.getString("address_portfolio_names" + i, DEFAULT);
            }
            else {
                // Older installations won't have the name saved.
                // REMOVE
                String serialString = sharedPreferences.getString("address_portfolio" + i, DEFAULT);
                AddressPortfolioObj addressPortfolioObj = DataBridge.deserialize(serialString, AddressPortfolioObj.class);
                name = addressPortfolioObj.name;

                // Save the name now so this never has to be done again.
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("address_portfolio_names" + i, name);
                editor.apply();
            }
            settings_address_portfolio_names.add(name);
        }
    }

    public void resetAllData() {
        settings_address_portfolio_names = new ArrayList<>();

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

        String sizeKey = "address_portfolio_size";
        int size = sharedPreferences.getInt(sizeKey, 0);
        o.serialize(sizeKey, size, Integer.class);

        for(int i = 0; i < size; i++) {
            String nameKey = "address_portfolio_names" + i;
            String serialNameString = sharedPreferences.getString(nameKey, DEFAULT);
            o.serialize(nameKey, serialNameString, String.class);

            String key = "address_portfolio" + i;
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

        String sizeKey = "address_portfolio_size";
        int size = o.deserialize(sizeKey, Integer.class);
        editor.putInt(sizeKey, size);

        for(int i = 0; i < size; i++) {
            String nameKey = "address_portfolio_names" + i;
            String nameValue = o.deserialize(nameKey, String.class);
            editor.putString(nameKey, nameValue);

            String key = "address_portfolio" + i;
            String value = o.deserialize(key, String.class);
            editor.putString(key, DataBridge.cycleSerialization(value, AddressPortfolioObj.class));
        }

        editor.apply();

        o.endObject();

        // Reinitialize data.
        loadAllData();
    }
}
