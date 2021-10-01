package com.musicslayer.cryptobuddy.util;

import com.musicslayer.cryptobuddy.persistence.Settings;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Calendar;
import java.util.Date;

public class DateTime {
    public static String toDateString(java.util.Date date, FormatStyle style) {
        if(date == null) { return null; }

        LocalDateTime localDateTime = LocalDateTime.ofInstant(date.toInstant(), TimeZoneManager.getSettingTimeZone());
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(style, style).withZone(TimeZoneManager.getSettingTimeZone()).withLocale(LocaleManager.getSettingLocaleDatetime());
        return localDateTime.format(dateTimeFormatter);
    }

    public static String toDateString(java.util.Date date) {
        return toDateString(date, Settings.setting_datetime);
    }

    public static String toDateString(FormatStyle style) {
        return toDateString(new java.util.Date(), style);
    }

    public static int compare(Date a, Date b) {
        boolean isValidA = a != null;
        boolean isValidB = b != null;

        // Null is always smaller than a real date.
        if(isValidA & isValidB) { return a.compareTo(b); }
        else { return Boolean.compare(isValidA, isValidB); }
    }

    public static Date getDateTime(int year, int month, int day, int hour, int minute, int second) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, hour, minute, second);
        return calendar.getTime();
    }

    // Crypto did not exist before 2000, so we consider only the range 2XXX.
    public static Date getMinDateTime() {
        return getDateTime(2000, Calendar.JANUARY, 1, 0, 0, 0);
    }

    public static Date getMaxDateTime() {
        return getDateTime(2099, Calendar.DECEMBER, 31, 23, 59, 59);
    }

    public static String serialize(Date d) {
        // Serialize a date as the number of milliseconds as a string.
        if(d == null) {
            return null;
        }
        else {
            return Long.toString(d.getTime());
        }
    }

    public static Date deserialize(String s) {
        // Input string should parse to be a long value.
        if(s == null || "null".equals(s)) {
            return null;
        }
        else {
            return new Date(Long.parseLong(s));
        }
    }
}
