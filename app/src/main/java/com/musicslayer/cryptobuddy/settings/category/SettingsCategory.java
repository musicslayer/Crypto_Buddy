package com.musicslayer.cryptobuddy.settings.category;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.settings.setting.Setting;
import com.musicslayer.cryptobuddy.util.FileUtil;
import com.musicslayer.cryptobuddy.util.ReflectUtil;

import java.util.ArrayList;
import java.util.HashMap;

abstract public class SettingsCategory {
    public static ArrayList<SettingsCategory> setting_categories;
    public static HashMap<String, SettingsCategory> settings_category_map;
    public static ArrayList<String> settings_category_names;
    public static ArrayList<String> settings_category_display_names;

    public static void initialize() {
        setting_categories = new ArrayList<>();
        settings_category_map = new HashMap<>();
        settings_category_display_names = new ArrayList<>();

        settings_category_names = FileUtil.readFileIntoLines(R.raw.settings_category);
        for(String settingsCategoryName : settings_category_names) {
            SettingsCategory settingsCategory = ReflectUtil.constructClassInstanceFromName("com.musicslayer.cryptobuddy.settings.category." + settingsCategoryName);

            setting_categories.add(settingsCategory);
            settings_category_map.put(settingsCategoryName, settingsCategory);
            settings_category_display_names.add(settingsCategory.getDisplayName());
        }
    }

    abstract public String getName();
    abstract public String getDisplayName();

    // The output order is what the user will see.
    abstract public ArrayList<Setting> getSettings();

    // For now, just use the name as the key.
    public String getKey() {
        return getName();
    }

    public static SettingsCategory getSettingsCategoryFromKey(String key) {
        SettingsCategory settingsCategory = settings_category_map.get(key);
        if(settingsCategory == null) {
            settingsCategory = UnknownSettingsCategory.createUnknownSettingsCategory(key);
        }

        return settingsCategory;
    }
}
