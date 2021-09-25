package com.musicslayer.cryptobuddy.util;

import android.app.Activity;

import androidx.appcompat.app.AppCompatDelegate;

import com.musicslayer.cryptobuddy.persistence.Settings;

public class Appearance {
    // Needs activity, not Context
    public static void setAppearance(Activity activity) {
        setTheme();
        setOrientation(activity);
    }

    public static void setTheme() {
        if(!Settings.setting_dark.equals(AppCompatDelegate.getDefaultNightMode())) {
            AppCompatDelegate.setDefaultNightMode(Settings.setting_dark);
        }
    }

    // Needs activity, not Context
    public static void setOrientation(Activity activity) {
        if(!Settings.setting_orientation.equals(activity.getResources().getConfiguration().orientation)) {
            activity.setRequestedOrientation(Settings.setting_orientation);
        }
    }
}
