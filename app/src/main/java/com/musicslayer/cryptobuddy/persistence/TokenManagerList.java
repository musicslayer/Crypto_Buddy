package com.musicslayer.cryptobuddy.persistence;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.musicslayer.cryptobuddy.asset.tokenmanager.TokenManager;
import com.musicslayer.cryptobuddy.serialize.Serialization;
import com.musicslayer.cryptobuddy.util.ThreadUtil;
import com.musicslayer.cryptobuddy.util.ThrowableUtil;

import java.util.HashMap;

// TODO, when we find new tokens, do we have to save everything!?

public class TokenManagerList {
    // Just pick something that would never actually be saved.
    public final static String DEFAULT = "!UNKNOWN!";

    // Store the raw TokenManager strings in case we need them in a data dump.
    // Once everything has successfully loaded we stop updating these.
    public static HashMap<String, String> token_manager_raw = new HashMap<>();

    public static void saveAllData(Context context) {
        //ThreadUtil.threadDump();

        SharedPreferences settings = context.getSharedPreferences("token_manager_data", MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.clear();

        for(TokenManager tokenManager : TokenManager.tokenManagers) {
            editor.putString("token_manager_" + tokenManager.getSettingsKey(), Serialization.serialize(tokenManager));
        }

        editor.apply();
    }

    public static void updateTokenManager(Context context, TokenManager tokenManager) {
        SharedPreferences settings = context.getSharedPreferences("token_manager_data", MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.putString("token_manager_" + tokenManager.getSettingsKey(), Serialization.serialize(tokenManager));
        editor.apply();
    }

    public static void initializeRawArray() {
        token_manager_raw = new HashMap<>();
    }

    public static TokenManager loadData(Context context, String settingsKey) {
        // TokenManager will create empty objects, but then this method will fill them in with data.
        // If a new TokenManager is introduced later, it will still be created but will get no data from here.
        SharedPreferences settings = context.getSharedPreferences("token_manager_data", MODE_PRIVATE);
        String serialString = settings.getString("token_manager_" + settingsKey, DEFAULT);
        token_manager_raw.put(settingsKey, serialString == null ? "null" : serialString);

        return DEFAULT.equals(serialString) ? null : Serialization.deserialize(serialString, TokenManager.class);
    }

    public static HashMap<String, String> getAllData() {
        HashMap<String, String> hashMap = new HashMap<>();

        for(String key : token_manager_raw.keySet()) {
            hashMap.put("RAW_" + key, token_manager_raw.get(key));
        }

        // We want the raw data even if this next piece errors.
        try {
            for(TokenManager tokenManager : TokenManager.tokenManagers) {
                hashMap.put("OBJ_" + tokenManager.getSettingsKey(), Serialization.serialize(tokenManager));
            }
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
        }

        return hashMap;
    }

    public static void resetAllData(Context context) {
        // Only reset data stored in settings, not the TokenManager class.
        SharedPreferences settings = context.getSharedPreferences("token_manager_data", MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.clear();
        editor.apply();
    }
}
