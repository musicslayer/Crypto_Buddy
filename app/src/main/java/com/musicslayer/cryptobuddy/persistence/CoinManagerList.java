package com.musicslayer.cryptobuddy.persistence;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

import com.musicslayer.cryptobuddy.asset.coinmanager.CoinManager;
import com.musicslayer.cryptobuddy.serialize.Serialization;

public class CoinManagerList {
    // Just pick something that would never actually be saved.
    public final static String DEFAULT = "!UNKNOWN!";

    public static String getSharedPreferencesKey() {
        return "coin_manager_data";
    }

    public static void saveAllData(Context context) {
        SharedPreferences settings = context.getSharedPreferences(getSharedPreferencesKey(), MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.clear();

        for(CoinManager coinManager : CoinManager.coinManagers) {
            editor.putString("coin_manager_" + coinManager.getSettingsKey(), Serialization.serialize(coinManager));
        }

        editor.apply();
    }

    public static void updateCoinManager(Context context, CoinManager coinManager) {
        SharedPreferences settings = context.getSharedPreferences(getSharedPreferencesKey(), MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.putString("coin_manager_" + coinManager.getSettingsKey(), Serialization.serialize(coinManager));
        editor.apply();
    }

    public static CoinManager loadData(Context context, String settingsKey) {
        // CoinManager will create empty objects, but then this method will fill them in with data.
        // If a new CoinManager is introduced later, it will still be created but will get no data from here.
        SharedPreferences settings = context.getSharedPreferences(getSharedPreferencesKey(), MODE_PRIVATE);
        String serialString = settings.getString("coin_manager_" + settingsKey, DEFAULT);
        return DEFAULT.equals(serialString) ? null : Serialization.deserialize(serialString, CoinManager.class);
    }

    public static void resetAllData(Context context) {
        // Only reset data stored in settings, not the TokenManager class.
        SharedPreferences settings = context.getSharedPreferences(getSharedPreferencesKey(), MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.clear();
        editor.apply();
    }

    //public String exportVersion() { return "1"; }

    public static boolean canExport() { return true; }

    public static String exportToJSON(Context context) throws org.json.JSONException {
        SharedPreferences settings = context.getSharedPreferences(getSharedPreferencesKey(), MODE_PRIVATE);

        Serialization.JSONObjectWithNull o = new Serialization.JSONObjectWithNull();

        for(CoinManager coinManager : CoinManager.coinManagers) {
            String key = "coin_manager_" + coinManager.getSettingsKey();
            String serialString = settings.getString(key, DEFAULT);

            // We do not want to export hardcoded coins, so let's remove them.
            String newSerialString;
            try {
                Serialization.JSONObjectWithNull oldJSON = new Serialization.JSONObjectWithNull(serialString);
                oldJSON.put("hardcoded_coins", new Serialization.JSONArrayWithNull());
                newSerialString = oldJSON.toStringOrNull();
            }
            catch(Exception e) {
                throw new IllegalStateException(e);
            }

            o.put(key, newSerialString);
        }

        return o.toStringOrNull();
    }


    public static void importFromJSON1(Context context, String s) throws org.json.JSONException {
        Serialization.JSONObjectWithNull o = new Serialization.JSONObjectWithNull(s);

        // Only import coin managers that currently exist.
        SharedPreferences settings = context.getSharedPreferences(getSharedPreferencesKey(), MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        for(CoinManager coinManager : CoinManager.coinManagers) {
            String key = "coin_manager_" + coinManager.getSettingsKey();
            if(o.has(key)) {
                String value = o.getString(key);
                if(!DEFAULT.equals(value)) {
                    editor.putString(key, Serialization.validate(value, CoinManager.class));
                }
            }
        }

        editor.apply();

        // Reinitialize data.
        CoinManager.initialize(context);
    }
}
