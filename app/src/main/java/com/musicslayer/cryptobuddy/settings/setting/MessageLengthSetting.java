package com.musicslayer.cryptobuddy.settings.setting;

import android.content.Context;
import android.widget.Toast;

import com.musicslayer.cryptobuddy.view.settings.MessageSettingsView;
import com.musicslayer.cryptobuddy.view.settings.SettingsView;

import java.util.ArrayList;

public class MessageLengthSetting extends Setting {
    public static Integer value;
    public void updateValue() { value = getSettingValue(); }

    public String getKey() { return "MessageLengthSetting"; }
    public String getName() { return "MessageLengthSetting"; }
    public String getDisplayName() { return "Message Length"; }
    public String getSettingsKey() { return "message"; }

    public ArrayList<String> getOptionNames() {
        ArrayList<String> optionNames = new ArrayList<>();
        optionNames.add("Short");
        optionNames.add("Long");
        return optionNames;
    }

    public String getDefaultOptionName() {
        return "Short";
    }

    public ArrayList<String> getOptionDisplays() {
        ArrayList<String> optionDisplays = new ArrayList<>();
        optionDisplays.add("Show messages for a shorter time.");
        optionDisplays.add("Show messages for a longer time.");
        return optionDisplays;
    }

    @SuppressWarnings("unchecked")
    public <T> ArrayList<T> getOptionValues() {
        ArrayList<Integer> optionValues = new ArrayList<>();
        optionValues.add(Toast.LENGTH_SHORT);
        optionValues.add(Toast.LENGTH_LONG);
        return (ArrayList<T>)optionValues;
    }

    public SettingsView createSettingView(Context context) {
        return new MessageSettingsView(context, this);
    }
}
