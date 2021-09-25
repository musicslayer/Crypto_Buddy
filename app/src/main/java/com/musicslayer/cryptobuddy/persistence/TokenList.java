package com.musicslayer.cryptobuddy.persistence;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

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

    public static void resetAllData(Context context) {
        resetDownloadedTokens(context);
        resetFoundTokens(context);
        resetCustomTokens(context);
    }
}
