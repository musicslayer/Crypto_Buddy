package com.musicslayer.cryptobuddy.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;

import androidx.appcompat.app.AppCompatDelegate;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.settings.setting.DarkModeSetting;
import com.musicslayer.cryptobuddy.settings.setting.OrientationSetting;

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

    public static int getPrimaryColor(Context context) {
        // Returns the primary color of the current theme.
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(R.attr.colorPrimary, typedValue, true);
        return typedValue.data;
    }

    public static int getSecondaryColor(Context context) {
        // Returns the secondary color of the current theme.
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(R.attr.colorSecondary, typedValue, true);
        return typedValue.data;
    }
}
