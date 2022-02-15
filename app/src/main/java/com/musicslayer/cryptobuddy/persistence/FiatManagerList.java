package com.musicslayer.cryptobuddy.persistence;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

import com.musicslayer.cryptobuddy.asset.fiatmanager.FiatManager;
import com.musicslayer.cryptobuddy.serialize.Serialization;

public class FiatManagerList {
    // Just pick something that would never actually be saved.
    public final static String DEFAULT = "!UNKNOWN!";

    public static String getSharedPreferencesKey() {
        return "fiat_manager_data";
    }

    public static void saveAllData(Context context) {
        SharedPreferences settings = context.getSharedPreferences(getSharedPreferencesKey(), MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.clear();

        for(FiatManager fiatManager : FiatManager.fiatManagers) {
            editor.putString("fiat_manager_" + fiatManager.getSettingsKey(), Serialization.serialize(fiatManager));
        }

        editor.apply();
    }

    public static void updateFiatManager(Context context, FiatManager fiatManager) {
        SharedPreferences settings = context.getSharedPreferences(getSharedPreferencesKey(), MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.putString("fiat_manager_" + fiatManager.getSettingsKey(), Serialization.serialize(fiatManager));
        editor.apply();
    }

    public static FiatManager loadData(Context context, String settingsKey) {
        // FiatManager will create empty objects, but then this method will fill them in with data.
        // If a new FiatManager is introduced later, it will still be created but will get no data from here.
        SharedPreferences settings = context.getSharedPreferences(getSharedPreferencesKey(), MODE_PRIVATE);
        String serialString = settings.getString("fiat_manager_" + settingsKey, DEFAULT);
        return DEFAULT.equals(serialString) ? null : Serialization.deserialize(serialString, FiatManager.class);
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

        for(FiatManager fiatManager : FiatManager.fiatManagers) {
            String key = "fiat_manager_" + fiatManager.getSettingsKey();
            String serialString = settings.getString(key, DEFAULT);
            o.put(key, serialString);
        }

        return o.toStringOrNull();
    }


    public static void importFromJSON1(Context context, String s) throws org.json.JSONException {
        Serialization.JSONObjectWithNull o = new Serialization.JSONObjectWithNull(s);

        // Only import fiat managers that currently exist.
        SharedPreferences settings = context.getSharedPreferences(getSharedPreferencesKey(), MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        for(FiatManager fiatManager : FiatManager.fiatManagers) {
            String key = "fiat_manager_" + fiatManager.getSettingsKey();
            if(o.has(key)) {
                String value = o.getString(key);
                if(!DEFAULT.equals(value)) {
                    // This round trip of deserializing and serializing ensures that the data is valid.
                    FiatManager dummyFiatManager = Serialization.deserialize(value, FiatManager.class);
                    editor.putString(key, Serialization.serialize(dummyFiatManager));
                }
            }
        }

        editor.apply();

        // Reinitialize data.
        FiatManager.initialize(context);
    }
}
