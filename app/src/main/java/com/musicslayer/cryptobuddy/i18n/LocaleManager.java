package com.musicslayer.cryptobuddy.i18n;

import com.musicslayer.cryptobuddy.persistence.Settings;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Locale;

public class LocaleManager {
    // A special Locale object meant to signify that we want to match the system default.
    final public static Locale MATCH_SYSTEM = new Locale("!", "!", "!");

    public static String formatPositiveNumber(String s) {
        // Format a positive number (String of a BigDecimal) based on the locale.
        Locale L = getSettingLocaleNumeric();
        if(L == null) {
            return s;
        }
        else {
            NumberFormat nf = NumberFormat.getInstance(L);
            DecimalFormat df = ((DecimalFormat)nf);
            DecimalFormatSymbols ds = df.getDecimalFormatSymbols();

            if("∞".equals(s)) {
                return ds.getInfinity();
            }
            else {
                BigDecimal D = new BigDecimal(s);

                String pattern = df.toLocalizedPattern();

                // We only deal with positive numbers here, so take first pattern.
                int idxSemicolon = pattern.indexOf(ds.getPatternSeparator());
                if(idxSemicolon != -1) {
                    pattern = pattern.substring(0, idxSemicolon);
                }

                return formatBase(D, pattern, L);
            }
        }
    }

    public static String formatNegativeNumber(String s) {
        // Format a negative number (String of a BigDecimal) based on the locale.
        // (The input String is always a positive number.)
        // Note that the user may wish to override how negative numbers are displayed, so we may not match the Locale.
        if(!"match_locale".equals(Settings.setting_loss) && !"red_match_locale".equals(Settings.setting_loss)) {
            return formatNegativeNumberOverride(s);
        }

        Locale L = getSettingLocaleNumeric();
        if(L == null) {
            // Without any Locale, just use regular minus sign.
            return "-" + s;
        }
        else {
            NumberFormat nf = NumberFormat.getInstance(L);
            DecimalFormat df = ((DecimalFormat)nf);
            DecimalFormatSymbols ds = df.getDecimalFormatSymbols();

            if("∞".equals(s)) {
                // We can't really apply a pattern to infinity, so just prepend the minus sign.
                return ds.getMinusSign() + ds.getInfinity();
            }
            else {
                BigDecimal D = new BigDecimal(s);

                String pattern = df.toLocalizedPattern();

                // We only deal with negative numbers here, so take second pattern.
                // If there is no second pattern, then use the first pattern and prepend the result with the minus sign.
                int idxSemicolon = pattern.indexOf(ds.getPatternSeparator());
                boolean shouldPrepend = idxSemicolon == -1;
                if(idxSemicolon != -1) {
                    pattern = pattern.substring(idxSemicolon + 1);
                }

                String newS = formatBase(D, pattern, L);
                if(shouldPrepend) {
                    newS = ds.getMinusSign() + newS;
                }

                return newS;
            }
        }
    }

    public static String formatBase(BigDecimal D, String pattern, Locale L) {
        NumberFormat nf = NumberFormat.getInstance(L);
        DecimalFormat df = ((DecimalFormat)nf);
        DecimalFormatSymbols ds = df.getDecimalFormatSymbols();

        char decimalSeparator_c = ds.getDecimalSeparator();
        String decimalSeparator = String.valueOf(decimalSeparator_c);

        int idx_decimal = pattern.lastIndexOf(decimalSeparator_c);
        if(idx_decimal != -1) {
            // Insert the exact number of decimal places in the pattern that we need, despite what the Locale says.
            char digit = ds.getDigit();
            char zeroDigit = ds.getZeroDigit();

            String pattern_front = pattern.substring(0, idx_decimal);

            String pattern_middle;
            if(D.scale() == 0) {
                // Don't keep decimal point, and we don't need any digits.
                pattern_middle = "";
            }
            else {
                pattern_middle = decimalSeparator + String.format("%0" + D.scale() + "d", 0).replace("0", String.valueOf(zeroDigit));
            }

            // Remove digits starting from the decimal point, but keep end symbols like parentheses.
            int idx_back = idx_decimal + 1;
            for(; idx_back < pattern.length(); idx_back++) {
                char c = pattern.charAt(idx_back);
                if(c != digit && c != zeroDigit) {
                    break;
                }
            }

            String pattern_back = pattern.substring(idx_back);

            // Use "format" because some locales (ccp) have characters ('\uD804' 55300) that are altered by concatenation.
            pattern = String.format("%s%s%s", pattern_front, pattern_middle, pattern_back);
            df.applyLocalizedPattern(pattern);
        }

        return df.format(D);
    }

    public static String formatNegativeNumberOverride(String s) {
        // Format the input based on the setting for loss values.
        // Note that color is applied in "AssetTextView".
        // The symbols used here are not based on any Locale.
        if("negative".equals(Settings.setting_loss) || "red_negative".equals(Settings.setting_loss)) {
            s = "-" + s;
        }

        if("parentheses".equals(Settings.setting_loss) || "red_parentheses".equals(Settings.setting_loss)) {
            s = "(" + s + ")";
        }

        return s;
    }

    public static Locale getSettingLocaleNumeric() {
        // Null means do not use any formatting.
        Locale L = Settings.setting_locale_numeric;
        if(MATCH_SYSTEM.equals(L)) {
            // Use the system default.
            L = Locale.getDefault();
        }

        return L;
    }

    public static Locale getSettingLocaleDatetime() {
        // Null means do not use any formatting.
        Locale L = Settings.setting_locale_datetime;
        if(MATCH_SYSTEM.equals(L)) {
            // Use the system default.
            L = Locale.getDefault();
        }
        return L;
    }

    public static ArrayList<Locale> getAvailableLocalesNumeric() {
        // Return all numeric locales, sorted by name.
        ArrayList<Locale> localeArrayList = new ArrayList<>(Arrays.asList(NumberFormat.getAvailableLocales()));

        Collections.sort(localeArrayList, (a, b) -> a.toString().compareToIgnoreCase(b.toString()));

        return localeArrayList;
    }

    public static ArrayList<Locale> getAvailableLocalesDatetime() {
        // Return all datetime locales, sorted by name.
        ArrayList<Locale> localeArrayList = new ArrayList<>(Arrays.asList(Calendar.getAvailableLocales()));

        Collections.sort(localeArrayList, (a, b) -> a.toString().compareToIgnoreCase(b.toString()));

        return localeArrayList;
    }
}
