package com.musicslayer.cryptobuddy.persistence;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

import com.musicslayer.cryptobuddy.asset.crypto.token.Token;
import com.musicslayer.cryptobuddy.asset.tokenmanager.TokenManager;

import java.util.HashMap;

public class TokenList {
    public final static String DEFAULT = "[]"; // Empty JSON Array

    public static void set(Context context, String settingsKey, String source, String json) {
        // The source should only be "downloaded", "found", or "custom".
        SharedPreferences settings = context.getSharedPreferences("token_list_data_" + source, MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(settingsKey, json);
        editor.apply();
    }

    public static String get(Context context, String settingsKey, String source) {
        // The source should only be "downloaded", "found", or "custom".
        SharedPreferences settings = context.getSharedPreferences("token_list_data_" + source, MODE_PRIVATE);
        return settings.getString(settingsKey, DEFAULT);
    }

    public static void resetDownloadedTokens(Context context) {
        SharedPreferences settings = context.getSharedPreferences("token_list_data_downloaded", MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.clear();
        editor.apply();
    }

    public static void resetFoundTokens(Context context) {
        SharedPreferences settings = context.getSharedPreferences("token_list_data_found", MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.clear();
        editor.apply();
    }

    public static void resetCustomTokens(Context context) {
        SharedPreferences settings = context.getSharedPreferences("token_list_data_custom", MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.clear();
        editor.apply();
    }

    public static HashMap<String, String> getAllData() {
        // This method is non-standard because of how TokenList and TokenManager interact.
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("token_list_data_downloaded", getDownloadedTokenString());
        hashMap.put("token_list_data_found", getFoundTokenString());
        hashMap.put("token_list_data_custom", getCustomTokenString());
        return hashMap;
    }

    public static void resetAllData(Context context) {
        resetDownloadedTokens(context);
        resetFoundTokens(context);
        resetCustomTokens(context);
    }

    private static String getDownloadedTokenString() {
        StringBuilder s = new StringBuilder();
        s.append("Downloaded Tokens:");

        for(TokenManager tokenManager : TokenManager.tokenManagers) {
            s.append("\n    ").append(tokenManager.getKey());
            for(Token token : tokenManager.downloaded_tokens) {
                s.append("\n      ").append(token.serializeToJSONX());
            }
        }

        return s.toString();
    }

    private static String getFoundTokenString() {
        StringBuilder s = new StringBuilder();
        s.append("Found Tokens:");

        for(TokenManager tokenManager : TokenManager.tokenManagers) {
            s.append("\n    ").append(tokenManager.getKey());
            for(Token token : tokenManager.found_tokens) {
                s.append("\n      ").append(token.serializeToJSONX());
            }
        }

        return s.toString();
    }

    private static String getCustomTokenString() {
        StringBuilder s = new StringBuilder();
        s.append("Custom Tokens:");

        for(TokenManager tokenManager : TokenManager.tokenManagers) {
            s.append("\n    ").append(tokenManager.getKey());
            for(Token token : tokenManager.custom_tokens) {
                s.append("\n      ").append(token.serializeToJSONX());
            }
        }

        return s.toString();
    }
}
