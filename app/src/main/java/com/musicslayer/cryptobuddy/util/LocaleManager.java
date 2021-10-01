package com.musicslayer.cryptobuddy.util;

import com.musicslayer.cryptobuddy.persistence.Settings;

import java.util.Locale;

public class LocaleManager {
    public static Locale getSettingLocaleNumeric() {
        // Null means that a number should not get any formatting.
        Locale L = Settings.setting_locale_numeric;
        if(new Locale("!", "!", "!").equals(L)) {
            // Use the system default.
            L = Locale.getDefault();
        }

        return L;
    }

    public static Locale getSettingLocaleDatetime() {
        // For now, this cannot be null.
        Locale L = Settings.setting_locale_datetime;
        if(new Locale("!", "!", "!").equals(L)) {
            // Use the system default.
            L = Locale.getDefault();
        }
        return L;
    }
}
