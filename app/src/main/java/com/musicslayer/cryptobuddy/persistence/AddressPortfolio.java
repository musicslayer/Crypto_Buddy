package com.musicslayer.cryptobuddy.persistence;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

import com.musicslayer.cryptobuddy.json.JSONWithNull;
import com.musicslayer.cryptobuddy.data.Serialization;

public class AddressPortfolio {
    // This default will cause an error when deserialized. We should never see this value used.
    public final static String DEFAULT = "null";

    public static ArrayList<String> settings_address_portfolio_names = new ArrayList<>();

    public static String getSharedPreferencesKey() {
        return "address_portfolio_data";
    }

    public static boolean isSaved(String name) {
        return settings_address_portfolio_names.contains(name);
    }

    public static AddressPortfolioObj getFromName(Context context, String name) {
        if(!isSaved(name)) {
            return null;
        }

        int idx = settings_address_portfolio_names.indexOf(name);

        SharedPreferences settings = context.getSharedPreferences(getSharedPreferencesKey(), MODE_PRIVATE);
        String serialString = settings.getString("address_portfolio" + idx, DEFAULT);
        return Serialization.deserialize(serialString, AddressPortfolioObj.class);
    }

    public static void loadAllData(Context context) {
        // Only load portfolio names. Portfolios themselves are loaded when needed.
        settings_address_portfolio_names = new ArrayList<>();

        SharedPreferences settings = context.getSharedPreferences(getSharedPreferencesKey(), MODE_PRIVATE);
        int size = settings.getInt("address_portfolio_size", 0);

        for(int i = 0; i < size; i++) {
            String name;
            if(settings.contains("address_portfolio_names" + i)) {
                name = settings.getString("address_portfolio_names" + i, DEFAULT);
            }
            else {
                // Older installations won't have the name saved.
                // TODO To Remove!
                String serialString = settings.getString("address_portfolio" + i, DEFAULT);
                AddressPortfolioObj addressPortfolioObj = Serialization.deserialize(serialString, AddressPortfolioObj.class);
                name = addressPortfolioObj.name;

                // Save the name now so this never has to be done again.
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("address_portfolio_names" + i, name);
                editor.apply();
            }
            settings_address_portfolio_names.add(name);
        }
    }

    public static void addPortfolio(Context context, AddressPortfolioObj addressPortfolioObj) {
        // Add this portfolio to the end and save data.
        settings_address_portfolio_names.add(addressPortfolioObj.name);

        SharedPreferences settings = context.getSharedPreferences(getSharedPreferencesKey(), MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        int size = settings_address_portfolio_names.size();
        editor.putInt("address_portfolio_size", size);
        editor.putString("address_portfolio" + (size - 1), Serialization.serialize(addressPortfolioObj));
        editor.putString("address_portfolio_names" + (size - 1), addressPortfolioObj.name);

        editor.apply();
    }

    public static void removePortfolio(Context context, String addressPortfolioObjName) {
        // Remove this portfolio, and then shift others to condense.
        int idx = settings_address_portfolio_names.indexOf(addressPortfolioObjName);
        settings_address_portfolio_names.remove(idx);

        SharedPreferences settings = context.getSharedPreferences(getSharedPreferencesKey(), MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        int size = settings.getInt("address_portfolio_size", 0);

        for(int i = idx; i < size - 1; i++) {
            String serialString = settings.getString("address_portfolio" + (i + 1), DEFAULT);
            String name = settings.getString("address_portfolio_names" + (i + 1), DEFAULT);

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

    public static void updatePortfolio(Context context, AddressPortfolioObj addressPortfolioObj) {
        SharedPreferences settings = context.getSharedPreferences(getSharedPreferencesKey(), MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        // We only need to update the portfolio object because the name can never change.
        int idx = settings_address_portfolio_names.indexOf(addressPortfolioObj.name);
        editor.putString("address_portfolio" + idx, Serialization.serialize(addressPortfolioObj));

        editor.apply();
    }

    public static void resetAllData(Context context) {
        settings_address_portfolio_names = new ArrayList<>();

        SharedPreferences settings = context.getSharedPreferences(getSharedPreferencesKey(), MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.clear();
        editor.apply();
    }

    //public String exportVersion() { return "1"; }

    public static boolean canExport() { return true; }

    public static String exportToJSON(Context context) throws org.json.JSONException {
        SharedPreferences settings = context.getSharedPreferences(getSharedPreferencesKey(), MODE_PRIVATE);

        JSONWithNull.JSONObjectWithNull o = new JSONWithNull.JSONObjectWithNull();

        String sizeKey = "address_portfolio_size";
        int size = settings.getInt(sizeKey, 0);
        o.put(sizeKey, Serialization.serialize(size));

        for(int i = 0; i < size; i++) {
            String nameKey = "address_portfolio_names" + i;
            String serialNameString = settings.getString(nameKey, DEFAULT);
            o.put(nameKey, serialNameString);

            String key = "address_portfolio" + i;
            String serialString = settings.getString(key, DEFAULT);
            o.put(key, serialString);
        }

        return o.toStringOrNull();
    }


    public static void importFromJSON1(Context context, String s) throws org.json.JSONException {
        JSONWithNull.JSONObjectWithNull o = new JSONWithNull.JSONObjectWithNull(s);

        SharedPreferences settings = context.getSharedPreferences(getSharedPreferencesKey(), MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        String sizeKey = "address_portfolio_size";
        int size = Serialization.deserialize(o.getString(sizeKey), Integer.class);
        editor.putInt(sizeKey, size);

        for(int i = 0; i < size; i++) {
            String nameKey = "address_portfolio_names" + i;
            String nameValue = o.getString(nameKey);
            editor.putString(nameKey, nameValue);

            String key = "address_portfolio" + i;
            String value = o.getString(key);
            editor.putString(key, Serialization.validate(value, AddressPortfolioObj.class));
        }

        editor.apply();

        // Reinitialize data.
        AddressPortfolio.loadAllData(context);
    }
}
