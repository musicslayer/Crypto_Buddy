package com.musicslayer.cryptobuddy.persistence;

import android.content.SharedPreferences;

import com.musicslayer.cryptobuddy.data.Exportation;
import com.musicslayer.cryptobuddy.util.SharedPreferencesUtil;

public class Review extends PersistentDataStore {
    public String getName() { return "Review"; }

    public boolean canExport() { return false; }
    public String doExport() { return Exportation.exportData(this, Review.class); }
    public void doImport(String s) { Exportation.importData(this, s, Review.class); }

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

    public void loadAllData() {
        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());

        // If there is no value in this setting, then immediately store "now" so that in all future calls there will be a value.
        // We cannot rely on a default value because it would not be fixed in time.
        if(!sharedPreferences.contains("review_time")) {
            setReviewTime();
        }
        settings_review_time = sharedPreferences.getLong("review_time", 0);
    }

    public void resetAllData() {
        settings_review_time = System.currentTimeMillis();

        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.clear();
        editor.apply();
    }
}
