package com.musicslayer.cryptobuddy.persistence;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Date;
import java.util.HashMap;

import static android.content.Context.MODE_PRIVATE;

public class Review {
    public static long settings_review_time;

    public static void setReviewTime(Context context) {
        // Set the new date to now.
        settings_review_time = new Date().getTime();

        SharedPreferences settings = context.getSharedPreferences("review_data", MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong("review_time", settings_review_time);
        editor.apply();
    }

    public static void disableReviewTime(Context context) {
        // Set the new date to a far away date so that the user will never be asked for a review again.
        settings_review_time = Long.MAX_VALUE;

        SharedPreferences settings = context.getSharedPreferences("review_data", MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong("review_time", settings_review_time);
        editor.apply();
    }

    public static void loadAllData(Context context) {
        SharedPreferences settings = context.getSharedPreferences("review_data", MODE_PRIVATE);

        // Default time is now, so that in 5 days all users will get the request for a view.
        settings_review_time = settings.getLong("review_time", new Date().getTime());
    }

    public static HashMap<String, String> getAllData() {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("settings_review_time", Long.toString(settings_review_time));
        return hashMap;
    }

    public static void resetAllData(Context context) {
        settings_review_time = new Date().getTime();

        SharedPreferences settings = context.getSharedPreferences("review_data", MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.clear();
        editor.apply();
    }
}
