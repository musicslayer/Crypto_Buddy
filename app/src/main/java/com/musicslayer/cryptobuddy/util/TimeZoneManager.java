package com.musicslayer.cryptobuddy.util;

import com.musicslayer.cryptobuddy.persistence.Settings;

import java.time.ZoneId;

public class TimeZoneManager {
    public static ZoneId getSettingTimeZone() {
        ZoneId Z = Settings.setting_time_zone;
        if(Z == null) {
            // Use the system default.
            Z = ZoneId.systemDefault();
        }
        return Z;
    }
}
