package com.musicslayer.cryptobuddy.settings.setting;

import com.musicslayer.cryptobuddy.i18n.LocaleManager;

import java.util.ArrayList;
import java.util.Locale;

public class NumericLocaleSetting extends Setting {
    public static Locale value;
    public void updateValue() { value = getSettingValue(); }

    public String getKey() { return "NumericLocaleSetting"; }
    public String getName() { return "NumericLocaleSetting"; }
    public String getDisplayName() { return "Numeric Locale"; }
    public String getSettingsKey() { return "locale_numeric"; }

    // Some options are hard coded, but more are added dynamically.
    public ArrayList<String> getOptionNames() {
        ArrayList<String> optionNames = new ArrayList<>();
        optionNames.add("Match System");
        optionNames.add("No Locale");

        for(Locale locale : LocaleManager.getAvailableLocalesNumeric()) {
            optionNames.add(locale.toString());
        }

        return optionNames;
    }

    public String getDefaultOptionName() {
        return "Match System";
    }

    public ArrayList<String> getOptionDisplays() {
        ArrayList<String> optionDisplays = new ArrayList<>();
        optionDisplays.add("Match system setting for numeric locale.");
        optionDisplays.add("Do not use a numeric locale.\nNumbers will appear in a raw decimal format (e.g., 12345.099).");

        for(Locale locale : LocaleManager.getAvailableLocalesNumeric()) {
            optionDisplays.add("Use " + locale.toString() + " numeric locale.");
        }

        return optionDisplays;
    }

    @SuppressWarnings("unchecked")
    public <T> ArrayList<T> getOptionValues() {
        ArrayList<Locale> optionValues = new ArrayList<>();
        optionValues.add(LocaleManager.MATCH_SYSTEM);
        optionValues.add(null);

        optionValues.addAll(LocaleManager.getAvailableLocalesNumeric());

        return (ArrayList<T>)optionValues;
    }
}
