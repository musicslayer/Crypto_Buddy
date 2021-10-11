package com.musicslayer.cryptobuddy.util;

import android.app.Activity;

import androidx.appcompat.app.AppCompatDelegate;

import com.musicslayer.cryptobuddy.settings.DarkModeSetting;
import com.musicslayer.cryptobuddy.settings.OrientationSetting;

public class AppearanceUtil {
    // Needs activity, not Context
    public static void setAppearance(Activity activity) {
        setTheme();
        setOrientation(activity);
    }

    public static void setTheme() {
        if(!DarkModeSetting.value.equals(AppCompatDelegate.getDefaultNightMode())) {
            AppCompatDelegate.setDefaultNightMode(DarkModeSetting.value);
        }
    }

    // Needs activity, not Context
    public static void setOrientation(Activity activity) {
        if(!OrientationSetting.value.equals(activity.getResources().getConfiguration().orientation)) {
            activity.setRequestedOrientation(OrientationSetting.value);
        }
    }
}
