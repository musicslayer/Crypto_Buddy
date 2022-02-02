package com.musicslayer.cryptobuddy.settings.setting;

import com.musicslayer.cryptobuddy.util.DateTimeUtil;

import java.time.format.FormatStyle;
import java.util.ArrayList;

public class DatetimeFormatSetting extends Setting {
    public static FormatStyle value;
    public void updateValue() { value = (FormatStyle)getOptionValues().get(chosenOptionPosition); }

    public String getKey() { return "DatetimeFormatSetting"; }
    public String getName() { return "DatetimeFormatSetting"; }
    public String getDisplayName() { return "Datetime Format"; }
    public String getSettingsKey() { return "datetime"; }

    public ArrayList<String> getOptionNames() {
        ArrayList<String> optionNames = new ArrayList<>();
        optionNames.add("Short");
        optionNames.add("Medium");
        optionNames.add("Long");
        optionNames.add("Full");
        return optionNames;
    }

    public String getDefaultOptionName() {
        return "Medium";
    }

    public ArrayList<String> getOptionDisplays() {
        ArrayList<String> optionDisplays = new ArrayList<>();
        optionDisplays.add("Short format datetime:\n" + DateTimeUtil.toDateString(FormatStyle.SHORT));
        optionDisplays.add("Medium format datetime:\n" + DateTimeUtil.toDateString(FormatStyle.MEDIUM));
        optionDisplays.add("Long format datetime:\n" + DateTimeUtil.toDateString(FormatStyle.LONG));
        optionDisplays.add("Full format datetime:\n" + DateTimeUtil.toDateString(FormatStyle.FULL));
        return optionDisplays;
    }

    @SuppressWarnings("unchecked")
    public <T> ArrayList<T> getOptionValues() {
        ArrayList<FormatStyle> optionValues = new ArrayList<>();
        optionValues.add(FormatStyle.SHORT);
        optionValues.add(FormatStyle.MEDIUM);
        optionValues.add(FormatStyle.LONG);
        optionValues.add(FormatStyle.FULL);
        return (ArrayList<T>)optionValues;
    }
}
