package com.musicslayer.cryptobuddy.persistence;

import android.content.Context;
import android.content.SharedPreferences;

import com.musicslayer.cryptobuddy.util.SharedPreferencesUtil;

public class Policy {
    public final static boolean DEFAULT_settings_privacy_policy = false;
    public static boolean settings_privacy_policy = false; // Did the user agree to the privacy policy?

    public final static boolean DEFAULT_settings_disclaimer = false;
    public static boolean settings_disclaimer = false; // Did the user agree to the disclaimer?

    public static String getSharedPreferencesKey() {
        return "policy_data";
    }

    public static boolean isAllPolicyAgreedTo() {
        return settings_privacy_policy && settings_disclaimer;
    }

    public static void setAgreePrivacyPolicy(Context context) {
        settings_privacy_policy = true;

        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("privacy_policy", settings_privacy_policy);
        editor.apply();
    }

    public static void setAgreeDisclaimer(Context context) {
        settings_disclaimer = true;

        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("disclaimer", settings_disclaimer);
        editor.apply();
    }

    public static void loadAllData(Context context) {
        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        settings_privacy_policy = sharedPreferences.getBoolean("privacy_policy", DEFAULT_settings_privacy_policy);
        settings_disclaimer = sharedPreferences.getBoolean("disclaimer", DEFAULT_settings_disclaimer);
    }

    public static void resetAllData(Context context) {
        settings_privacy_policy = DEFAULT_settings_privacy_policy;
        settings_disclaimer = DEFAULT_settings_disclaimer;

        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.clear();
        editor.apply();
    }

    public static boolean canExport() {
        return false;
    }
}
