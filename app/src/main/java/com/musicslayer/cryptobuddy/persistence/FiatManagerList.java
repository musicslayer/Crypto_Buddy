package com.musicslayer.cryptobuddy.persistence;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

import com.musicslayer.cryptobuddy.app.App;
import com.musicslayer.cryptobuddy.asset.fiatmanager.FiatManager;
import com.musicslayer.cryptobuddy.data.Exportation;
import com.musicslayer.cryptobuddy.json.JSONWithNull;
import com.musicslayer.cryptobuddy.data.Serialization;

public class FiatManagerList implements Exportation.ExportableToJSON, Exportation.Versionable {
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
            editor.putString("fiat_manager_" + fiatManager.getSettingsKey(), Serialization.serialize(fiatManager, FiatManager.class));
        }

        editor.apply();
    }

    public static void updateFiatManager(Context context, FiatManager fiatManager) {
        SharedPreferences settings = context.getSharedPreferences(getSharedPreferencesKey(), MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.putString("fiat_manager_" + fiatManager.getSettingsKey(), Serialization.serialize(fiatManager, FiatManager.class));
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

    public static boolean canExport() {
        return true;
    }

    public String exportationVersion() {
        return "1";
    }

    public static String exportDataToJSON() throws org.json.JSONException {
        SharedPreferences settings = App.applicationContext.getSharedPreferences(getSharedPreferencesKey(), MODE_PRIVATE);

        JSONWithNull.JSONObjectWithNull o = new JSONWithNull.JSONObjectWithNull();

        for(FiatManager fiatManager : FiatManager.fiatManagers) {
            String key = "fiat_manager_" + fiatManager.getSettingsKey();
            String serialString = settings.getString(key, DEFAULT);

            // We do not want to export hardcoded fiats, so let's remove them.
            String newSerialString;
            try {
                JSONWithNull.JSONObjectWithNull oldJSON = new JSONWithNull.JSONObjectWithNull(serialString);
                oldJSON.putJSONArray("hardcoded_fiats", new JSONWithNull.JSONArrayWithNull());
                newSerialString = oldJSON.toStringOrNull();
            }
            catch(Exception e) {
                throw new IllegalStateException(e);
            }

            o.put(key, newSerialString, String.class);
        }

        return o.toStringOrNull();
    }


    public static void importDataFromJSON(String s, String version) throws org.json.JSONException {
        JSONWithNull.JSONObjectWithNull o = new JSONWithNull.JSONObjectWithNull(s);

        // Only import fiat managers that currently exist.
        SharedPreferences settings = App.applicationContext.getSharedPreferences(getSharedPreferencesKey(), MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        for(FiatManager fiatManager : FiatManager.fiatManagers) {
            String key = "fiat_manager_" + fiatManager.getSettingsKey();
            if(o.has(key)) {
                String value = o.get(key, String.class);
                if(!DEFAULT.equals(value)) {
                    editor.putString(key, Serialization.validate(value, FiatManager.class));
                }
            }
        }

        editor.apply();

        // Reinitialize data.
        FiatManager.initialize(App.applicationContext);
    }
}
