package com.musicslayer.cryptobuddy.util;

import android.app.Activity;

import androidx.appcompat.app.AppCompatDelegate;

import com.musicslayer.cryptobuddy.settings.Setting;

public class AppearanceUtil {
    // Needs activity, not Context
    public static void setAppearance(Activity activity) {
        setTheme();
        setOrientation(activity);
    }

    public static void setTheme() {
        if(!Setting.getSettingValueFromKey("DarkModeSetting").equals(AppCompatDelegate.getDefaultNightMode())) {
            AppCompatDelegate.setDefaultNightMode(Setting.getSettingValueFromKey("DarkModeSetting"));
        }
    }

    // Needs activity, not Context
    public static void setOrientation(Activity activity) {
        if(!Setting.getSettingValueFromKey("OrientationSetting").equals(activity.getResources().getConfiguration().orientation)) {
            activity.setRequestedOrientation(Setting.getSettingValueFromKey("OrientationSetting"));
        }
    }
}
