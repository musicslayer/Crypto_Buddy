package com.musicslayer.cryptobuddy.persistence;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

import java.util.HashMap;

public class PrivacyPolicy {
    public static boolean settings_privacy_policy = false; // Did user agree to the privacy policy?

    public static void setAgree(Context context) {
        settings_privacy_policy = true;

        SharedPreferences settings = context.getSharedPreferences("privacy_policy_data", MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("privacy_policy", settings_privacy_policy);
        editor.apply();
    }

    public static void loadAllData(Context context) {
        SharedPreferences settings = context.getSharedPreferences("privacy_policy_data", MODE_PRIVATE);
        settings_privacy_policy = settings.getBoolean("privacy_policy", false);
    }

    // Used for debugging and testing
    public static void resetAllData(Context context) {
        settings_privacy_policy = false;

        SharedPreferences settings = context.getSharedPreferences("privacy_policy_data", MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.clear();
        editor.putBoolean("privacy_policy", false);
        editor.apply();
    }

    public static HashMap<String, String> getDataDump() {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("settings_privacy_policy", Boolean.toString(settings_privacy_policy));
        return hashMap;
    }
}
