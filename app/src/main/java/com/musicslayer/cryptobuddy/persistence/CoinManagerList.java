package com.musicslayer.cryptobuddy.persistence;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

import com.musicslayer.cryptobuddy.asset.coinmanager.CoinManager;
import com.musicslayer.cryptobuddy.serialize.Serialization;
import com.musicslayer.cryptobuddy.util.ThrowableUtil;

import java.util.HashMap;

public class CoinManagerList {
    // Just pick something that would never actually be saved.
    public final static String DEFAULT = "!UNKNOWN!";

    // Store the raw CoinManager strings in case we need them in a data dump.
    // Once everything has successfully loaded we stop updating these.
    public static HashMap<String, String> coin_manager_raw = new HashMap<>();

    public static void saveAllData(Context context) {
        SharedPreferences settings = context.getSharedPreferences("coin_manager_data", MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.clear();

        for(CoinManager coinManager : CoinManager.coinManagers) {
            editor.putString("coin_manager_" + coinManager.getSettingsKey(), Serialization.serialize(coinManager));
        }

        editor.apply();
    }

    public static void updateCoinManager(Context context, CoinManager coinManager) {
        SharedPreferences settings = context.getSharedPreferences("coin_manager_data", MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.putString("coin_manager_" + coinManager.getSettingsKey(), Serialization.serialize(coinManager));
        editor.apply();
    }

    public static void initializeRawArray() {
        coin_manager_raw = new HashMap<>();
    }

    public static CoinManager loadData(Context context, String settingsKey) {
        // CoinManager will create empty objects, but then this method will fill them in with data.
        // If a new CoinManager is introduced later, it will still be created but will get no data from here.
        SharedPreferences settings = context.getSharedPreferences("coin_manager_data", MODE_PRIVATE);
        String serialString = settings.getString("coin_manager_" + settingsKey, DEFAULT);
        coin_manager_raw.put(settingsKey, serialString == null ? "null" : serialString);

        return DEFAULT.equals(serialString) ? null : Serialization.deserialize(serialString, CoinManager.class);
    }

    public static HashMap<String, String> getAllData() {
        HashMap<String, String> hashMap = new HashMap<>();

        for(String key : coin_manager_raw.keySet()) {
            hashMap.put("RAW_" + key, coin_manager_raw.get(key));
        }

        // We want the raw data even if this next piece errors.
        try {
            for(CoinManager coinManager : CoinManager.coinManagers) {
                hashMap.put("OBJ_" + coinManager.getSettingsKey(), Serialization.serialize(coinManager));
            }
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
        }

        return hashMap;
    }

    public static void resetAllData(Context context) {
        // Only reset data stored in settings, not the TokenManager class.
        SharedPreferences settings = context.getSharedPreferences("coin_manager_data", MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.clear();
        editor.apply();
    }
}
