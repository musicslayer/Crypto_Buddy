package com.musicslayer.cryptobuddy.persistence;

import android.content.Context;
import android.content.SharedPreferences;

import com.musicslayer.cryptobuddy.app.App;
import com.musicslayer.cryptobuddy.data.Exportation;
import com.musicslayer.cryptobuddy.json.JSONWithNull;
import com.musicslayer.cryptobuddy.data.Serialization;
import com.musicslayer.cryptobuddy.util.SharedPreferencesUtil;

import java.util.ArrayList;

public class ExchangePortfolio implements Exportation.ExportableToJSON, Exportation.Versionable {
    // This default will cause an error when deserialized. We should never see this value used.
    public final static String DEFAULT = "null";

    public static ArrayList<String> settings_exchange_portfolio_names = new ArrayList<>();

    public static String getSharedPreferencesKey() {
        return "exchange_portfolio_data";
    }

    public static boolean isSaved(String name) {
        return settings_exchange_portfolio_names.contains(name);
    }

    public static ExchangePortfolioObj getFromName(Context context, String name) {
        if(!isSaved(name)) {
            return null;
        }

        int idx = settings_exchange_portfolio_names.indexOf(name);

        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        String serialString = sharedPreferences.getString("exchange_portfolio" + idx, DEFAULT);
        return Serialization.deserialize(serialString, ExchangePortfolioObj.class);
    }

    public static void loadAllData(Context context) {
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
                // TODO To Remove!
                String serialString = sharedPreferences.getString("exchange_portfolio" + i, DEFAULT);
                ExchangePortfolioObj exchangePortfolioObj = Serialization.deserialize(serialString, ExchangePortfolioObj.class);
                name = exchangePortfolioObj.name;

                // Save the name now so this never has to be done again.
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("exchange_portfolio_names" + i, name);
                editor.apply();
            }
            settings_exchange_portfolio_names.add(name);
        }
    }

    public static void addPortfolio(Context context, ExchangePortfolioObj exchangePortfolioObj) {
        // Add this portfolio to the end and save data.
        settings_exchange_portfolio_names.add(exchangePortfolioObj.name);

        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        int size = settings_exchange_portfolio_names.size();
        editor.putInt("exchange_portfolio_size", size);
        editor.putString("exchange_portfolio" + (size - 1), Serialization.serialize(exchangePortfolioObj, ExchangePortfolioObj.class));
        editor.putString("exchange_portfolio_names" + (size - 1), exchangePortfolioObj.name);

        editor.apply();
    }

    public static void removePortfolio(Context context, String exchangePortfolioObjName) {
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

    public static void updatePortfolio(Context context, ExchangePortfolioObj exchangePortfolioObj) {
        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // We only need to update the portfolio object because the name can never change.
        int idx = settings_exchange_portfolio_names.indexOf(exchangePortfolioObj.name);
        editor.putString("exchange_portfolio" + idx, Serialization.serialize(exchangePortfolioObj, ExchangePortfolioObj.class));

        editor.apply();
    }

    public static void resetAllData(Context context) {
        settings_exchange_portfolio_names = new ArrayList<>();

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

        String sizeKey = "exchange_portfolio_size";
        int size = sharedPreferences.getInt(sizeKey, 0);
        o.put(sizeKey, size, Integer.class);

        for(int i = 0; i < size; i++) {
            String nameKey = "exchange_portfolio_names" + i;
            String serialNameString = sharedPreferences.getString(nameKey, DEFAULT);
            o.put(nameKey, serialNameString, String.class);

            String key = "exchange_portfolio" + i;
            String serialString = sharedPreferences.getString(key, DEFAULT);
            o.put(key, serialString, String.class);
        }

        return o.toStringOrNull();
    }


    public static void importDataFromJSON(String s, String version) throws org.json.JSONException {
        JSONWithNull.JSONObjectWithNull o = new JSONWithNull.JSONObjectWithNull(s);

        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        String sizeKey = "exchange_portfolio_size";
        int size = o.get(sizeKey, Integer.class);
        editor.putInt(sizeKey, size);

        for(int i = 0; i < size; i++) {
            String nameKey = "exchange_portfolio_names" + i;
            String nameValue = o.get(nameKey, String.class);
            editor.putString(nameKey, nameValue);

            String key = "exchange_portfolio" + i;
            String value = o.get(key, String.class);
            editor.putString(key, Serialization.validate(value, ExchangePortfolioObj.class));
        }

        editor.apply();

        // Reinitialize data.
        ExchangePortfolio.loadAllData(App.applicationContext);
    }
}
