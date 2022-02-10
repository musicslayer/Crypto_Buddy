package com.musicslayer.cryptobuddy.persistence;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

import com.musicslayer.cryptobuddy.serialize.Serialization;
import com.musicslayer.cryptobuddy.util.ThrowableUtil;

import java.util.ArrayList;
import java.util.HashMap;

public class ExchangePortfolio {
    // This default will cause an error when deserialized. We should never see this value used.
    public final static String DEFAULT = "null";

    public static ArrayList<String> settings_exchange_portfolio_names = new ArrayList<>();

    public static boolean isSaved(String name) {
        return settings_exchange_portfolio_names.contains(name);
    }

    public static ExchangePortfolioObj getFromName(Context context, String name) {
        if(!isSaved(name)) {
            return null;
        }

        int idx = settings_exchange_portfolio_names.indexOf(name);

        SharedPreferences settings = context.getSharedPreferences("exchange_portfolio_data", MODE_PRIVATE);
        String serialString = settings.getString("exchange_portfolio" + idx, DEFAULT);
        return Serialization.deserialize(serialString, ExchangePortfolioObj.class);
    }

    public static void loadAllData(Context context) {
        // Only load portfolio names. Portfolios themselves are loaded when needed.
        // TODO People who used the app before won't have names saved.
        settings_exchange_portfolio_names = new ArrayList<>();

        SharedPreferences settings = context.getSharedPreferences("exchange_portfolio_data", MODE_PRIVATE);
        int size = settings.getInt("exchange_portfolio_size", 0);

        for(int i = 0; i < size; i++) {
            String name = settings.getString("exchange_portfolio_names" + i, DEFAULT);
            settings_exchange_portfolio_names.add(name);
        }
    }

    public static void addPortfolio(Context context, ExchangePortfolioObj exchangePortfolioObj) {
        // Add this portfolio to the end and save data.
        settings_exchange_portfolio_names.add(exchangePortfolioObj.name);

        SharedPreferences settings = context.getSharedPreferences("exchange_portfolio_data", MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        int size = settings_exchange_portfolio_names.size();
        editor.putInt("exchange_portfolio_size", size);
        editor.putString("exchange_portfolio" + (size - 1), Serialization.serialize(exchangePortfolioObj));
        editor.putString("exchange_portfolio_names" + (size - 1), exchangePortfolioObj.name);

        editor.apply();
    }

    // TODO Can we just pass in the name?
    public static void removePortfolio(Context context, ExchangePortfolioObj exchangePortfolioObj) {
        // Remove this portfolio, and then shift others to condense.
        int idx = settings_exchange_portfolio_names.indexOf(exchangePortfolioObj.name);
        settings_exchange_portfolio_names.remove(idx);

        SharedPreferences settings = context.getSharedPreferences("exchange_portfolio_data", MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        int size = settings.getInt("exchange_portfolio_size", 0);

        for(int i = idx; i < size - 1; i++) {
            String serialString = settings.getString("exchange_portfolio" + (i + 1), DEFAULT);
            String name = settings.getString("exchange_portfolio_names" + (i + 1), DEFAULT);

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
        SharedPreferences settings = context.getSharedPreferences("exchange_portfolio_data", MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        // We only need to update the portfolio object because the name can never change.
        int idx = settings_exchange_portfolio_names.indexOf(exchangePortfolioObj.name);
        editor.putString("exchange_portfolio" + idx, Serialization.serialize(exchangePortfolioObj));

        editor.apply();
    }

    public static HashMap<String, String> getAllData() {
        HashMap<String, String> hashMap = new HashMap<>();

        // TODO
        /*
        for(int key : settings_exchange_portfolio_raw.keySet()) {
            if(key == -1) {
                hashMap.put("SIZE", settings_exchange_portfolio_raw.get(key));
            }
            else {
                hashMap.put("RAW" + key, settings_exchange_portfolio_raw.get(key));
            }
        }

        // We want the raw data even if this next piece errors.
        try {
            for(int i = 0; i < settings_exchange_portfolio.size(); i++) {
                ExchangePortfolioObj exchangePortfolioObj = settings_exchange_portfolio.get(i);
                hashMap.put("OBJ" + i, Serialization.serialize(exchangePortfolioObj));
            }
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
        }

         */

        return hashMap;
    }

    public static void resetAllData(Context context) {
        settings_exchange_portfolio_names = new ArrayList<>();

        SharedPreferences settings = context.getSharedPreferences("exchange_portfolio_data", MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.clear();
        editor.apply();
    }
}
