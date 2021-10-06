package com.musicslayer.cryptobuddy.i18n;

import com.musicslayer.cryptobuddy.persistence.Settings;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

public class TimeZoneManager {
    public static ZoneId getSettingTimeZone() {
        ZoneId Z = Settings.setting_time_zone;
        if(Z == null) {
            // Use the system default.
            Z = ZoneId.systemDefault();
        }
        return Z;
    }

    public static ArrayList<String> getAvailableTimeZoneStrings() {
        // Return the time zone strings directly, sorted by time zone offset for "now".
        ArrayList<ZoneId> zoneIdArrayList = new ArrayList<>();

        for(String zoneIdString : ZoneId.getAvailableZoneIds()) {
            zoneIdArrayList.add(ZoneId.of(zoneIdString));
        }

        Instant nowInstant = new Date().toInstant();

        Collections.sort(zoneIdArrayList, (a, b) -> {
            // Sort by the offset first, then by name.
            int s = a.getRules().getOffset(nowInstant).compareTo(b.getRules().getOffset(nowInstant));
            if(s != 0) {
                return s;
            }
            else {
                return a.toString().compareToIgnoreCase(b.toString());
            }
        });

        ArrayList<String> zoneIdStringArrayList = new ArrayList<>();
        for(ZoneId zoneId : zoneIdArrayList) {
            zoneIdStringArrayList.add(zoneId.toString() + " (" + zoneId.getRules().getOffset(nowInstant).toString() + ")");
        }

        return zoneIdStringArrayList;
    }
}
