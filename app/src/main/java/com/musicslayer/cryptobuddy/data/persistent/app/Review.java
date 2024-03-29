package com.musicslayer.cryptobuddy.data.persistent.app;

import android.content.SharedPreferences;

import com.musicslayer.cryptobuddy.util.SharedPreferencesUtil;

public class Review extends PersistentAppDataStore {
    public String getName() { return "Review"; }

    public boolean isVisible() { return false; }
    public String doExport() { return null; }
    public void doImport(String s) {}

    // The default value is a dynamic date, so we can't store it here.
    public static long settings_review_time;

    public String getSharedPreferencesKey() {
        return "review_data";
    }

    public void setReviewTime() {
        // Set the new date to now.
        settings_review_time = System.currentTimeMillis();

        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong("review_time", settings_review_time);
        editor.apply();
    }

    public void disableReviewTime() {
        // Set the new date to a far away date so that the user will never be asked for a review again.
        settings_review_time = Long.MAX_VALUE;

        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong("review_time", settings_review_time);
        editor.apply();
    }

    public void saveAllData() {
        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putLong("review_time", settings_review_time);

        editor.apply();
    }

    public void loadAllData() {
        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());

        // If there is no value in this setting, then use "now" as the default.
        settings_review_time = sharedPreferences.getLong("review_time", System.currentTimeMillis());
    }

    public void resetAllData() {
        settings_review_time = System.currentTimeMillis();

        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.clear();
        editor.apply();
    }
}
