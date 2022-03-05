package com.musicslayer.cryptobuddy.util;

import com.musicslayer.cryptobuddy.i18n.LocaleManager;
import com.musicslayer.cryptobuddy.i18n.TimeZoneManager;
import com.musicslayer.cryptobuddy.settings.setting.DatetimeFormatSetting;

import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateTimeUtil {
    public static String toDateString(java.util.Date date, FormatStyle style) {
        if(date == null) { return null; }

        Locale L = LocaleManager.getSettingLocaleDatetime();
        if(L == null) {
            // Just return the long value.
            return Long.toString(date.getTime());
        }
        else {
            LocalDateTime localDateTime = LocalDateTime.ofInstant(date.toInstant(), TimeZoneManager.getSettingTimeZone());
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(style, style).withZone(TimeZoneManager.getSettingTimeZone());
            dateTimeFormatter = dateTimeFormatter.withLocale(L);
            return localDateTime.format(dateTimeFormatter);
        }
    }

    public static String toDateString(java.util.Date date) {
        return toDateString(date, DatetimeFormatSetting.value);
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

    public static Date parseStandard(String s) throws ParseException {
        // If the time ends with Z, set the timezone to UTC.
        // If the time ends with a nonzero offset, set the timezone to GMC + the offset.
        // Note that we cannot parse with "X" because Android does not handle this correctly.
        Date date;

        if(s.endsWith("Z")) {
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
            format.setTimeZone(TimeZone.getTimeZone("UTC"));
            date = format.parse(s);
        }
        else {
            int offsetIdx = 19; // Just hardcode this.
            String block_time_main = s.substring(0, offsetIdx);
            String block_time_offset = s.substring(offsetIdx);
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);
            format.setTimeZone(TimeZone.getTimeZone("GMT" + block_time_offset));
            date = format.parse(block_time_main);
        }

        return date;
    }

    public static Date parseExtended(String s) throws ParseException {
        // If the time ends with Z, set the timezone to UTC.
        // If the time ends with a nonzero offset, set the timezone to GMC + the offset.
        // Note that we cannot parse with "X" because Android does not handle this correctly.
        Date date;

        if(s.endsWith("Z")) {
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss'Z'", Locale.ENGLISH);
            format.setTimeZone(TimeZone.getTimeZone("UTC"));
            date = format.parse(s);
        }
        else {
            int offsetIdx = 23; // Just hardcode this.
            String block_time_main = s.substring(0, offsetIdx);
            String block_time_offset = s.substring(offsetIdx);
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss", Locale.ENGLISH);
            format.setTimeZone(TimeZone.getTimeZone("GMT" + block_time_offset));
            date = format.parse(block_time_main);
        }

        return date;
    }

    public static Date parseMilliseconds(String s) {
        // Input is the time since the Unix Epoch in milliseconds.
        BigInteger i = new BigInteger(s);
        return new Date(i.longValue());
    }

    public static Date parseSeconds(String s) {
        // Input is the time since the Unix Epoch in seconds.
        BigInteger i = new BigInteger(s);
        i = i.multiply(new BigInteger("1000"));
        return new Date(i.longValue());
    }

    public static Date parseRippleSeconds(String s) {
        // Input is the time since the Ripple Epoch in seconds.
        //The Ripple Epoch is 946684800 seconds after the Unix Epoch
        BigInteger i = new BigInteger(s);
        i = i.add(new BigInteger("946684800"));
        i = i.multiply(new BigInteger("1000"));
        return new Date(i.longValue());
    }
}
