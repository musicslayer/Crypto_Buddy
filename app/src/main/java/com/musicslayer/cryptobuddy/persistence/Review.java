package com.musicslayer.cryptobuddy.persistence;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

public class Review {
    // The default value is a dynamic date, so we can't store it here.
    public static long settings_review_time;

    public static String getSharedPreferencesKey() {
        return "review_data";
    }

    public static void setReviewTime(Context context) {
        // Set the new date to now.
        settings_review_time = System.currentTimeMillis();

        SharedPreferences settings = context.getSharedPreferences(getSharedPreferencesKey(), MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong("review_time", settings_review_time);
        editor.apply();
    }

    public static void disableReviewTime(Context context) {
        // Set the new date to a far away date so that the user will never be asked for a review again.
        settings_review_time = Long.MAX_VALUE;

        SharedPreferences settings = context.getSharedPreferences(getSharedPreferencesKey(), MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong("review_time", settings_review_time);
        editor.apply();
    }

    public static void loadAllData(Context context) {
        SharedPreferences settings = context.getSharedPreferences(getSharedPreferencesKey(), MODE_PRIVATE);

        // If there is no value in this setting, then immediately store "now" so that in all future calls there will be a value.
        // We cannot rely on a default value because it would not be fixed in time.
        if(!settings.contains("review_time")) {
            setReviewTime(context);
        }
        settings_review_time = settings.getLong("review_time", 0);
    }

    public static void resetAllData(Context context) {
        settings_review_time = System.currentTimeMillis();

        SharedPreferences settings = context.getSharedPreferences(getSharedPreferencesKey(), MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.clear();
        editor.apply();
    }
}
