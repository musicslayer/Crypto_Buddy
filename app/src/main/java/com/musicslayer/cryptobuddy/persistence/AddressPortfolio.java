package com.musicslayer.cryptobuddy.persistence;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.HashMap;

import static android.content.Context.MODE_PRIVATE;

import com.musicslayer.cryptobuddy.util.ThrowableLogger;
import com.musicslayer.cryptobuddy.util.Serialization;

public class AddressPortfolio {
    // Store the raw strings too in case we need them in a data dump.
    public static ArrayList<String> settings_address_portfolio_raw = new ArrayList<>();
    public static ArrayList<AddressPortfolioObj> settings_address_portfolio = new ArrayList<>();

    public static boolean isSaved(String name) {
        for(AddressPortfolioObj p : settings_address_portfolio) {
            if(name.equals(p.name)) {
                return true;
            }
        }
        return false;
    }

    public static void saveAllData(Context context) {
        SharedPreferences settings = context.getSharedPreferences("address_portfolio_data", MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.clear();

        int size = settings_address_portfolio.size();
        editor.putInt("address_portfolio_size", size);

        for(int i = 0; i < size; i++) {
            AddressPortfolioObj addressPortfolioObj = settings_address_portfolio.get(i);
            editor.putString("address_portfolio" + i, Serialization.serialize(addressPortfolioObj));
        }

        editor.apply();
    }

    public static void loadAllData(Context context) {
        settings_address_portfolio = new ArrayList<>();

        SharedPreferences settings = context.getSharedPreferences("address_portfolio_data", MODE_PRIVATE);
        int size = settings.getInt("address_portfolio_size", 0);

        for(int i = 0; i < size; i++) {
            String serialString = settings.getString("address_portfolio" + i, "");
            settings_address_portfolio_raw.add(serialString);

            AddressPortfolioObj addressPortfolioObj = Serialization.deserialize(serialString, AddressPortfolioObj.class);

            // If there is any problem at all, don't add this one.
            if(addressPortfolioObj != null) {
                settings_address_portfolio.add(addressPortfolioObj);
            }
        }

        // Data might have changed if portfolios removed cryptos that no longer exist.
        saveAllData(context);
    }

    public static void addPortfolio(Context context, AddressPortfolioObj addressPortfolioObj) {
        settings_address_portfolio.add(addressPortfolioObj);

        AddressPortfolio.saveAllData(context);
    }

    public static void removePortfolio(Context context, AddressPortfolioObj addressPortfolioObj) {
        settings_address_portfolio.remove(addressPortfolioObj);

        AddressPortfolio.saveAllData(context);
    }

    public static AddressPortfolioObj getFromName(String name) {
        for(AddressPortfolioObj p : settings_address_portfolio) {
            if(name.equals(p.name)) {
                return p;
            }
        }
        return null;
    }

    public static HashMap<String, String> getAllData() {
        HashMap<String, String> hashMap = new HashMap<>();
        for(int i = 0; i < settings_address_portfolio_raw.size(); i++) {
            hashMap.put("RAW" + i, settings_address_portfolio_raw.get(i));
        }

        // We want the raw data even if this next piece errors.
        try {
            for(int i = 0; i < settings_address_portfolio.size(); i++) {
                AddressPortfolioObj addressPortfolioObj = settings_address_portfolio.get(i);
                hashMap.put("OBJ" + i, Serialization.serialize(addressPortfolioObj));
            }
        }
        catch(Exception e) {
            ThrowableLogger.processThrowable(e);
        }

        return hashMap;
    }

    public static void resetAllData(Context context) {
        settings_address_portfolio = new ArrayList<>();

        SharedPreferences settings = context.getSharedPreferences("address_portfolio_data", MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.clear();
        editor.apply();
    }
}
