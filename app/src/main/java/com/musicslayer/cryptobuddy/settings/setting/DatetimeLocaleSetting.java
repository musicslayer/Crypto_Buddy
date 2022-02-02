package com.musicslayer.cryptobuddy.settings.setting;

import com.musicslayer.cryptobuddy.i18n.LocaleManager;

import java.util.ArrayList;
import java.util.Locale;

public class DatetimeLocaleSetting extends Setting {
    public static Locale value;
    public void updateValue() { value = (Locale)getSettingValue(); }

    public String getKey() { return "DatetimeLocaleSetting"; }
    public String getName() { return "DatetimeLocaleSetting"; }
    public String getDisplayName() { return "Datetime Locale"; }
    public String getSettingsKey() { return "locale_datetime"; }

    // Some options are hard coded, but more are added dynamically.
    public ArrayList<String> getOptionNames() {
        ArrayList<String> optionNames = new ArrayList<>();
        optionNames.add("Match System");
        optionNames.add("No Locale");

        for(Locale locale : LocaleManager.getAvailableLocalesDatetime()) {
            optionNames.add(locale.toString());
        }

        return optionNames;
    }

    public String getDefaultOptionName() {
        return "Match System";
    }

    public ArrayList<String> getOptionDisplays() {
        ArrayList<String> optionDisplays = new ArrayList<>();
        optionDisplays.add("Match system setting for datetime locale.");
        optionDisplays.add("Do not use a datetime locale.\nDatetimes will display as the number of milliseconds since January 1, 1970, 00:00:00 GMT");

        for(Locale locale : LocaleManager.getAvailableLocalesDatetime()) {
            optionDisplays.add("Use " + locale.toString() + " datetime locale.");
        }

        return optionDisplays;
    }

    @SuppressWarnings("unchecked")
    public <T> ArrayList<T> getOptionValues() {
        ArrayList<Locale> optionValues = new ArrayList<>();
        optionValues.add(LocaleManager.MATCH_SYSTEM);
        optionValues.add(null);

        optionValues.addAll(LocaleManager.getAvailableLocalesDatetime());

        return (ArrayList<T>)optionValues;
    }
}
