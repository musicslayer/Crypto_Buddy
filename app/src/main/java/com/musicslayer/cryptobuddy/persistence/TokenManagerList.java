package com.musicslayer.cryptobuddy.persistence;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

import com.musicslayer.cryptobuddy.asset.tokenmanager.TokenManager;
import com.musicslayer.cryptobuddy.json.JSONWithNull;
import com.musicslayer.cryptobuddy.data.Serialization;

public class TokenManagerList {
    // Just pick something that would never actually be saved.
    public final static String DEFAULT = "!UNKNOWN!";

    public static String getSharedPreferencesKey() {
        return "token_manager_data";
    }

    public static void saveAllData(Context context) {
        SharedPreferences settings = context.getSharedPreferences(getSharedPreferencesKey(), MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.clear();

        for(TokenManager tokenManager : TokenManager.tokenManagers) {
            editor.putString("token_manager_" + tokenManager.getSettingsKey(), Serialization.serialize(tokenManager));
        }

        editor.apply();
    }

    public static void updateTokenManager(Context context, TokenManager tokenManager) {
        SharedPreferences settings = context.getSharedPreferences(getSharedPreferencesKey(), MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.putString("token_manager_" + tokenManager.getSettingsKey(), Serialization.serialize(tokenManager));
        editor.apply();
    }

    public static TokenManager loadData(Context context, String settingsKey) {
        // TokenManager will create empty objects, but then this method will fill them in with data.
        // If a new TokenManager is introduced later, it will still be created but will get no data from here.
        SharedPreferences settings = context.getSharedPreferences(getSharedPreferencesKey(), MODE_PRIVATE);
        String serialString = settings.getString("token_manager_" + settingsKey, DEFAULT);
        return DEFAULT.equals(serialString) ? null : Serialization.deserialize(serialString, TokenManager.class);
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

        JSONWithNull.JSONObjectWithNull o = new JSONWithNull.JSONObjectWithNull();

        for(TokenManager tokenManager : TokenManager.tokenManagers) {
            String key = "token_manager_" + tokenManager.getSettingsKey();
            String serialString = settings.getString(key, DEFAULT);

            // We do not want to export downloaded tokens, so let's remove them.
            String newSerialString;
            try {
                JSONWithNull.JSONObjectWithNull oldJSON = new JSONWithNull.JSONObjectWithNull(serialString);
                oldJSON.put("downloaded_tokens", new JSONWithNull.JSONArrayWithNull());
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
        JSONWithNull.JSONObjectWithNull o = new JSONWithNull.JSONObjectWithNull(s);

        // Only import token managers that currently exist.
        SharedPreferences settings = context.getSharedPreferences(getSharedPreferencesKey(), MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        for(TokenManager tokenManager : TokenManager.tokenManagers) {
            String key = "token_manager_" + tokenManager.getSettingsKey();
            if(o.has(key)) {
                String value = o.getString(key);
                if(!DEFAULT.equals(value)) {
                    editor.putString(key, Serialization.validate(value, TokenManager.class));
                }
            }
        }

        editor.apply();

        // Reinitialize data.
        TokenManager.initialize(context);
    }
}
