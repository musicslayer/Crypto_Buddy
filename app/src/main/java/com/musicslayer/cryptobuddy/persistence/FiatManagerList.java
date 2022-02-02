package com.musicslayer.cryptobuddy.persistence;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

import com.musicslayer.cryptobuddy.asset.fiatmanager.FiatManager;
import com.musicslayer.cryptobuddy.serialize.Serialization;
import com.musicslayer.cryptobuddy.util.ThrowableUtil;

import java.util.HashMap;

public class FiatManagerList {
    // Just pick something that would never actually be saved.
    public final static String DEFAULT = "!UNKNOWN!";

    // Store the raw FiatManager strings in case we need them in a data dump.
    // Once everything has successfully loaded we stop updating these.
    public static HashMap<String, String> fiat_manager_raw = new HashMap<>();

    public static void saveAllData(Context context) {
        SharedPreferences settings = context.getSharedPreferences("fiat_manager_data", MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.clear();

        for(FiatManager fiatManager : FiatManager.fiatManagers) {
            editor.putString("fiat_manager_" + fiatManager.getSettingsKey(), Serialization.serialize(fiatManager));
        }

        editor.apply();
    }

    public static void updateFiatManager(Context context, FiatManager fiatManager) {
        SharedPreferences settings = context.getSharedPreferences("fiat_manager_data", MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.putString("fiat_manager_" + fiatManager.getSettingsKey(), Serialization.serialize(fiatManager));
        editor.apply();
    }

    public static void initializeRawArray() {
        fiat_manager_raw = new HashMap<>();
    }

    public static FiatManager loadData(Context context, String settingsKey) {
        // FiatManager will create empty objects, but then this method will fill them in with data.
        // If a new FiatManager is introduced later, it will still be created but will get no data from here.
        SharedPreferences settings = context.getSharedPreferences("fiat_manager_data", MODE_PRIVATE);
        String serialString = settings.getString("fiat_manager_" + settingsKey, DEFAULT);
        fiat_manager_raw.put(settingsKey, serialString == null ? "null" : serialString);

        return DEFAULT.equals(serialString) ? null : Serialization.deserialize(serialString, FiatManager.class);
    }

    public static HashMap<String, String> getAllData() {
        HashMap<String, String> hashMap = new HashMap<>();

        for(String key : fiat_manager_raw.keySet()) {
            hashMap.put("RAW_" + key, fiat_manager_raw.get(key));
        }

        // We want the raw data even if this next piece errors.
        try {
            for(FiatManager fiatManager : FiatManager.fiatManagers) {
                hashMap.put("OBJ_" + fiatManager.getSettingsKey(), Serialization.serialize(fiatManager));
            }
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
        }

        return hashMap;
    }

    public static void resetAllData(Context context) {
        // Only reset data stored in settings, not the TokenManager class.
        SharedPreferences settings = context.getSharedPreferences("fiat_manager_data", MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.clear();
        editor.apply();
    }
}
