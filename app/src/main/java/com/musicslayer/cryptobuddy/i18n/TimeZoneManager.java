package com.musicslayer.cryptobuddy.i18n;

import com.musicslayer.cryptobuddy.settings.setting.TimeZoneSetting;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;

public class TimeZoneManager {
    // Set this date when the app starts in app initialization.
    public static Instant nowInstant;

    public static ZoneId getSettingTimeZone() {
        ZoneId Z = TimeZoneSetting.value;
        if(Z == null) {
            // Use the system default.
            Z = ZoneId.systemDefault();
        }
        return Z;
    }

    public static ArrayList<ZoneId> getAvailableTimeZones() {
        // Return the time zone strings directly, sorted by time zone offset for "now".
        ArrayList<ZoneId> zoneIdArrayList = new ArrayList<>();

        for(String zoneIdString : ZoneId.getAvailableZoneIds()) {
            zoneIdArrayList.add(ZoneId.of(zoneIdString));
        }

        sortByOffset(zoneIdArrayList);

        return zoneIdArrayList;
    }

    private static void sortByOffset(ArrayList<ZoneId> zoneIdArrayList) {
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
    }
}
