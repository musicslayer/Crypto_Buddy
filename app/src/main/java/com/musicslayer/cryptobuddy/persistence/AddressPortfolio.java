package com.musicslayer.cryptobuddy.persistence;

import android.content.SharedPreferences;

import java.util.ArrayList;

import com.musicslayer.cryptobuddy.data.Exportation;
import com.musicslayer.cryptobuddy.json.JSONWithNull;
import com.musicslayer.cryptobuddy.data.Serialization;
import com.musicslayer.cryptobuddy.util.SharedPreferencesUtil;

public class AddressPortfolio extends PersistentDataStore implements Exportation.ExportableToJSON, Exportation.Versionable {
    public String getName() { return "AddressPortfolio"; }

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
        return Serialization.deserialize(serialString, AddressPortfolioObj.class);
    }

    public void addPortfolio(AddressPortfolioObj addressPortfolioObj) {
        // Add this portfolio to the end and save data.
        settings_address_portfolio_names.add(addressPortfolioObj.name);

        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        int size = settings_address_portfolio_names.size();
        editor.putInt("address_portfolio_size", size);
        editor.putString("address_portfolio" + (size - 1), Serialization.serialize(addressPortfolioObj, AddressPortfolioObj.class));
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
        editor.putString("address_portfolio" + idx, Serialization.serialize(addressPortfolioObj, AddressPortfolioObj.class));

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
                // TODO To Remove!
                String serialString = sharedPreferences.getString("address_portfolio" + i, DEFAULT);
                AddressPortfolioObj addressPortfolioObj = Serialization.deserialize(serialString, AddressPortfolioObj.class);
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

    public boolean canExport() {
        return true;
    }

    public String exportationVersion() {
        return "1";
    }

    public String exportDataToJSON() throws org.json.JSONException {
        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());

        JSONWithNull.JSONObjectWithNull o = new JSONWithNull.JSONObjectWithNull();

        String sizeKey = "address_portfolio_size";
        int size = sharedPreferences.getInt(sizeKey, 0);
        o.put(sizeKey, size, Integer.class);

        for(int i = 0; i < size; i++) {
            String nameKey = "address_portfolio_names" + i;
            String serialNameString = sharedPreferences.getString(nameKey, DEFAULT);
            o.put(nameKey, serialNameString, String.class);

            String key = "address_portfolio" + i;
            String serialString = sharedPreferences.getString(key, DEFAULT);
            o.put(key, serialString, String.class);
        }

        return o.toStringOrNull();
    }


    public void importDataFromJSON(String s, String version) throws org.json.JSONException {
        JSONWithNull.JSONObjectWithNull o = new JSONWithNull.JSONObjectWithNull(s);

        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        String sizeKey = "address_portfolio_size";
        int size = o.get(sizeKey, Integer.class);
        editor.putInt(sizeKey, size);

        for(int i = 0; i < size; i++) {
            String nameKey = "address_portfolio_names" + i;
            String nameValue = o.get(nameKey, String.class);
            editor.putString(nameKey, nameValue);

            String key = "address_portfolio" + i;
            String value = o.get(key, String.class);
            editor.putString(key, Serialization.validate(value, AddressPortfolioObj.class));
        }

        editor.apply();

        // Reinitialize data.
        loadAllData();
    }
}
