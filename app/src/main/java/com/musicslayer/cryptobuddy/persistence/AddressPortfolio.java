package com.musicslayer.cryptobuddy.persistence;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.HashMap;

import static android.content.Context.MODE_PRIVATE;

import com.musicslayer.cryptobuddy.util.ThrowableUtil;
import com.musicslayer.cryptobuddy.serialize.Serialization;

public class AddressPortfolio {
    // This default will cause an error when deserialized. We should never see this value used.
    public final static String DEFAULT = "null";

    // Store the raw strings too in case we need them in a data dump.
    // Once everything has successfully loaded we stop updating these.
    public static HashMap<Integer, String> settings_address_portfolio_raw = new HashMap<>();

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
        settings_address_portfolio_raw = new HashMap<>();
        settings_address_portfolio = new ArrayList<>();

        SharedPreferences settings = context.getSharedPreferences("address_portfolio_data", MODE_PRIVATE);
        int size = settings.getInt("address_portfolio_size", 0);

        settings_address_portfolio_raw.put(-1, Integer.toString(size));

        for(int i = 0; i < size; i++) {
            String serialString = settings.getString("address_portfolio" + i, DEFAULT);
            settings_address_portfolio_raw.put(i, serialString == null ? "null" : serialString);

            AddressPortfolioObj addressPortfolioObj = Serialization.deserialize(serialString, AddressPortfolioObj.class);
            settings_address_portfolio.add(addressPortfolioObj);
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

        for(int key : settings_address_portfolio_raw.keySet()) {
            if(key == -1) {
                hashMap.put("SIZE", settings_address_portfolio_raw.get(key));
            }
            else {
                hashMap.put("RAW" + key, settings_address_portfolio_raw.get(key));
            }
        }

        // We want the raw data even if this next piece errors.
        try {
            for(int i = 0; i < settings_address_portfolio.size(); i++) {
                AddressPortfolioObj addressPortfolioObj = settings_address_portfolio.get(i);
                hashMap.put("OBJ" + i, Serialization.serialize(addressPortfolioObj));
            }
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
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
