package com.musicslayer.cryptobuddy.persistence;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

import com.musicslayer.cryptobuddy.serialize.Serialization;

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
        // TODO People who used the app before won't have names saved.
        settings_address_portfolio_names = new ArrayList<>();

        SharedPreferences settings = context.getSharedPreferences(getSharedPreferencesKey(), MODE_PRIVATE);
        int size = settings.getInt("address_portfolio_size", 0);

        for(int i = 0; i < size; i++) {
            String name = settings.getString("address_portfolio_names" + i, DEFAULT);
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

    // TODO Can we just pass in the name?
    public static void removePortfolio(Context context, AddressPortfolioObj addressPortfolioObj) {
        // Remove this portfolio, and then shift others to condense.
        int idx = settings_address_portfolio_names.indexOf(addressPortfolioObj.name);
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
}
