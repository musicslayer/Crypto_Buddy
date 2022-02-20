package com.musicslayer.cryptobuddy.data.persistent.app;

import android.content.SharedPreferences;

import com.musicslayer.cryptobuddy.data.bridge.Exportation;
import com.musicslayer.cryptobuddy.util.SharedPreferencesUtil;

public class Policy extends PersistentAppDataStore {
    public String getName() { return "Policy"; }

    public boolean canExport() { return false; }
    public String doExport() { return Exportation.exportData(this, Policy.class); }
    public void doImport(String s) { Exportation.importData(this, s, Policy.class); }

    public final static boolean DEFAULT_settings_privacy_policy = false;
    public static boolean settings_privacy_policy = false; // Did the user agree to the privacy policy?

    public final static boolean DEFAULT_settings_disclaimer = false;
    public static boolean settings_disclaimer = false; // Did the user agree to the disclaimer?

    public String getSharedPreferencesKey() {
        return "policy_data";
    }

    public static boolean isAllPolicyAgreedTo() {
        return settings_privacy_policy && settings_disclaimer;
    }

    public void setAgreePrivacyPolicy() {
        settings_privacy_policy = true;

        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("privacy_policy", settings_privacy_policy);
        editor.apply();
    }

    public void setAgreeDisclaimer() {
        settings_disclaimer = true;

        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("disclaimer", settings_disclaimer);
        editor.apply();
    }

    public void saveAllData() {
        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean("privacy_policy", settings_privacy_policy);
        editor.putBoolean("disclaimer", settings_disclaimer);

        editor.apply();
    }

    public void loadAllData() {
        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        settings_privacy_policy = sharedPreferences.getBoolean("privacy_policy", DEFAULT_settings_privacy_policy);
        settings_disclaimer = sharedPreferences.getBoolean("disclaimer", DEFAULT_settings_disclaimer);
    }

    public void resetAllData() {
        settings_privacy_policy = DEFAULT_settings_privacy_policy;
        settings_disclaimer = DEFAULT_settings_disclaimer;

        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.clear();
        editor.apply();
    }
}
