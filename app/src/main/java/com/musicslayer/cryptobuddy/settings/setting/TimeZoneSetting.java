package com.musicslayer.cryptobuddy.settings.setting;

import com.musicslayer.cryptobuddy.i18n.TimeZoneManager;

import java.time.ZoneId;
import java.util.ArrayList;

public class TimeZoneSetting extends Setting {
    public static ZoneId value;
    public void updateValue() { value = (ZoneId)getSettingValue(); }

    public String getKey() { return "TimeZoneSetting"; }
    public String getName() { return "TimeZoneSetting"; }
    public String getDisplayName() { return "Time Zone"; }
    public String getSettingsKey() { return "time_zone"; }

    public String modifyOptionName(String optionName) {
        // Each option except for the first is a zoneId String that we want to add the offset to.
        if("Match System".equals(optionName)) {
            return optionName;
        }
        else {
            ZoneId zoneId = ZoneId.of(optionName);
            return zoneId.toString() + " (" + zoneId.getRules().getOffset(TimeZoneManager.nowInstant).toString() + ")";
        }
    }

    // Some options are hard coded, but more are added dynamically.
    public ArrayList<String> getOptionNames() {
        ArrayList<String> optionNames = new ArrayList<>();
        optionNames.add("Match System");

        ArrayList<ZoneId> zoneIdArrayList = TimeZoneManager.getAvailableTimeZones();
        for(ZoneId zoneId : zoneIdArrayList) {
            optionNames.add(zoneId.toString());
        }

        return optionNames;
    }

    public String getDefaultOptionName() {
        return "Match System";
    }

    public ArrayList<String> getOptionDisplays() {
        ArrayList<String> optionDisplays = new ArrayList<>();
        optionDisplays.add("Match system setting for the time zone.");

        for(String zoneIdString : getModifiedOptionNames()) {
            if("Match System".equals(zoneIdString)) {
                continue;
            }

            optionDisplays.add("Use " + zoneIdString + " time zone.");
        }

        return optionDisplays;
    }

    @SuppressWarnings("unchecked")
    public <T> ArrayList<T> getOptionValues() {
        ArrayList<ZoneId> optionValues = new ArrayList<>();
        optionValues.add(null);

        optionValues.addAll(TimeZoneManager.getAvailableTimeZones());

        return (ArrayList<T>)optionValues;
    }
}
