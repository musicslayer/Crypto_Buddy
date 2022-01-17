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

    // Store the raw strings too in case we need them in a data dump.
    // Once everything has successfully loaded we stop updating these.
    public static HashMap<Integer, String> settings_exchange_portfolio_raw = new HashMap<>();

    public static ArrayList<ExchangePortfolioObj> settings_exchange_portfolio = new ArrayList<>();

    public static boolean isSaved(String name) {
        for(ExchangePortfolioObj p : settings_exchange_portfolio) {
            if(name.equals(p.name)) {
                return true;
            }
        }
        return false;
    }

    public static void saveAllData(Context context) {
        SharedPreferences settings = context.getSharedPreferences("exchange_portfolio_data", MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.clear();

        int size = settings_exchange_portfolio.size();
        editor.putInt("exchange_portfolio_size", size);

        for(int i = 0; i < size; i++) {
            ExchangePortfolioObj exchangePortfolioObj = settings_exchange_portfolio.get(i);
            editor.putString("exchange_portfolio" + i, Serialization.serialize(exchangePortfolioObj));
        }

        editor.apply();
    }

    public static void loadAllData(Context context) {
        settings_exchange_portfolio_raw = new HashMap<>();
        settings_exchange_portfolio = new ArrayList<>();

        SharedPreferences settings = context.getSharedPreferences("exchange_portfolio_data", MODE_PRIVATE);
        int size = settings.getInt("exchange_portfolio_size", 0);

        settings_exchange_portfolio_raw.put(-1, Integer.toString(size));

        for(int i = 0; i < size; i++) {
            String serialString = settings.getString("exchange_portfolio" + i, DEFAULT);
            settings_exchange_portfolio_raw.put(i, serialString == null ? "null" : serialString);

            ExchangePortfolioObj exchangePortfolioObj = Serialization.deserialize(serialString, ExchangePortfolioObj.class);
            settings_exchange_portfolio.add(exchangePortfolioObj);
        }
    }

    public static void addPortfolio(Context context, ExchangePortfolioObj exchangePortfolioObj) {
        settings_exchange_portfolio.add(exchangePortfolioObj);
        ExchangePortfolio.saveAllData(context);
    }

    public static void removePortfolio(Context context, ExchangePortfolioObj exchangePortfolioObj) {
        settings_exchange_portfolio.remove(exchangePortfolioObj);
        ExchangePortfolio.saveAllData(context);
    }

    public static void updatePortfolio(Context context, ExchangePortfolioObj exchangePortfolioObj) {
        SharedPreferences settings = context.getSharedPreferences("exchange_portfolio_data", MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        int idx = settings_exchange_portfolio.indexOf(exchangePortfolioObj);
        editor.putString("exchange_portfolio" + idx, Serialization.serialize(exchangePortfolioObj));

        editor.apply();
    }

    public static ExchangePortfolioObj getFromName(String name) {
        for(ExchangePortfolioObj p : settings_exchange_portfolio) {
            if(name.equals(p.name)) {
                return p;
            }
        }
        return null;
    }

    public static HashMap<String, String> getAllData() {
        HashMap<String, String> hashMap = new HashMap<>();

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

        return hashMap;
    }

    public static void resetAllData(Context context) {
        settings_exchange_portfolio = new ArrayList<>();

        SharedPreferences settings = context.getSharedPreferences("exchange_portfolio_data", MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.clear();
        editor.apply();
    }
}
