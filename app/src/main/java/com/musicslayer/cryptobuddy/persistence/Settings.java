package com.musicslayer.cryptobuddy.persistence;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;

import java.time.ZoneId;
import java.time.format.FormatStyle;
import java.util.HashMap;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;

// TODO All of this should be cleaned up...

public class Settings {
    public static FormatStyle setting_datetime;
    public static String setting_decimal;
    public static String setting_price;
    public static String setting_network;
    public static Integer setting_timeout;
    public static String setting_loss;
    public static Integer setting_dark;
    public static Integer setting_message;
    public static Integer setting_orientation;
    public static Boolean setting_confirm;
    public static String setting_asset;
    public static Integer setting_max_transactions;
    public static Locale setting_locale_numeric;
    public static Locale setting_locale_datetime;
    public static ZoneId setting_time_zone;

    public static int value_datetime = 0;
    public static int value_decimal = 0;
    public static int value_price = 0;
    public static int value_network = 0;
    public static int value_timeout = 0;
    public static int value_loss = 0;
    public static int value_dark = 0;
    public static int value_message = 0;
    public static int value_orientation = 0;
    public static int value_confirm = 0;
    public static int value_asset = 0;
    public static int value_max_transactions = 0;
    public static int value_locale_numeric = 0;
    public static int value_locale_datetime = 0;
    public static int value_time_zone = 0;

    public static HashMap<Integer, FormatStyle> map_datetime;
    static {
        map_datetime = new HashMap<>();
        map_datetime.put(0, FormatStyle.SHORT);
        map_datetime.put(1, FormatStyle.MEDIUM);
        map_datetime.put(2, FormatStyle.LONG);
        map_datetime.put(3, FormatStyle.FULL);
    }

    public static HashMap<Integer, String> map_decimal;
    static {
        map_decimal = new HashMap<>();
        map_decimal.put(0, "Fixed");
        map_decimal.put(1, "Truncated");
    }

    public static HashMap<Integer, String> map_price;
    static {
        map_price = new HashMap<>();
        map_price.put(0, "Forward");
        map_price.put(1, "ForwardBackward");
    }

    public static HashMap<Integer, String> map_network;
    static {
        map_network = new HashMap<>();
        map_network.put(0, "All");
        map_network.put(1, "Mainnet");
    }

    public static HashMap<Integer, Integer> map_timeout;
    static {
        map_timeout = new HashMap<>();
        map_timeout.put(0, 10000);
        map_timeout.put(1, 30000);
        map_timeout.put(2, 60000);
    }

    public static HashMap<Integer, String> map_loss;
    static {
        map_loss = new HashMap<>();
        map_loss.put(0, "negative");
        map_loss.put(1, "red");
        map_loss.put(2, "parenthesis");
        map_loss.put(3, "red_negative");
        map_loss.put(4, "red_parenthesis");
    }

    public static HashMap<Integer, Integer> map_dark;
    static {
        map_dark = new HashMap<>();
        map_dark.put(0, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        map_dark.put(1, AppCompatDelegate.MODE_NIGHT_YES);
        map_dark.put(2, AppCompatDelegate.MODE_NIGHT_NO);
    }

    public static HashMap<Integer, Integer> map_message;
    static {
        map_message = new HashMap<>();
        map_message.put(0, Toast.LENGTH_SHORT);
        map_message.put(1, Toast.LENGTH_LONG);
    }

    public static HashMap<Integer, Integer> map_orientation;
    static {
        map_orientation = new HashMap<>();
        map_orientation.put(0, ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        map_orientation.put(1, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        map_orientation.put(2, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    public static HashMap<Integer, Boolean> map_confirm;
    static {
        map_confirm = new HashMap<>();
        map_confirm.put(0, true);
        map_confirm.put(1, false);
    }

    public static HashMap<Integer, String> map_asset;
    static {
        map_asset = new HashMap<>();
        map_asset.put(0, "full");
        map_asset.put(1, "symbol");
    }

    public static HashMap<Integer, Integer> map_max_transactions;
    static {
        map_max_transactions = new HashMap<>();
        map_max_transactions.put(0, 500);
        map_max_transactions.put(1, 1000);
        map_max_transactions.put(2, 5000);
    }

    public static HashMap<Integer, Locale> map_locale_numeric;
    static {
        map_locale_numeric = new HashMap<>();
        map_locale_numeric.put(0, new Locale("!", "!", "!")); // Match System.
        map_locale_numeric.put(1, null);
        map_locale_numeric.put(2, Locale.US);
        map_locale_numeric.put(3, Locale.FRENCH);
    }

    public static HashMap<Integer, Locale> map_locale_datetime;
    static {
        map_locale_datetime = new HashMap<>();
        map_locale_datetime.put(0, new Locale("!", "!", "!"));
        map_locale_datetime.put(1, Locale.US);
        map_locale_datetime.put(2, Locale.FRENCH);
    }

    public static HashMap<Integer, ZoneId> map_time_zone;
    static {
        map_time_zone = new HashMap<>();
        map_time_zone.put(0, null);
        map_time_zone.put(1, ZoneId.of("UTC"));
        map_time_zone.put(2, ZoneId.of("America/New_York"));
    }

    public static void setSetting(Context context, String settingName, int settingValue) {
        // If setting value has changed, make new value locally available now, and then write to persistent storage.
        switch(settingName) {
            case "datetime":
                if(value_datetime == settingValue){return;}
                value_datetime = settingValue;
                setting_datetime = map_datetime.get(settingValue);
                break;
            case "decimal":
                if(value_decimal == settingValue){return;}
                value_decimal = settingValue;
                setting_decimal = map_decimal.get(settingValue);
                break;
            case "price":
                if(value_price == settingValue){return;}
                value_price = settingValue;
                setting_price = map_price.get(settingValue);
                break;
            case "network":
                if(value_network == settingValue){return;}
                value_network = settingValue;
                setting_network = map_network.get(settingValue);
                break;
            case "timeout":
                if(value_timeout == settingValue){return;}
                value_timeout = settingValue;
                setting_timeout = map_timeout.get(settingValue);
                break;
            case "loss":
                if(value_loss == settingValue){return;}
                value_loss = settingValue;
                setting_loss = map_loss.get(settingValue);
                break;
            case "dark":
                if(value_dark == settingValue){return;}
                value_dark = settingValue;
                setting_dark = map_dark.get(settingValue);
                break;
            case "message":
                if(value_message == settingValue){return;}
                value_message = settingValue;
                setting_message = map_message.get(settingValue);
                break;
            case "orientation":
                if(value_orientation == settingValue){return;}
                value_orientation = settingValue;
                setting_orientation = map_orientation.get(settingValue);
                break;
            case "confirm":
                if(value_confirm == settingValue){return;}
                value_confirm = settingValue;
                setting_confirm = map_confirm.get(settingValue);
                break;
            case "asset":
                if(value_asset == settingValue){return;}
                value_asset = settingValue;
                setting_asset = map_asset.get(settingValue);
                break;
            case "max_transactions":
                if(value_max_transactions == settingValue){return;}
                value_max_transactions = settingValue;
                setting_max_transactions = map_max_transactions.get(settingValue);
                break;
            case "locale_numeric":
                if(value_locale_numeric == settingValue){return;}
                value_locale_numeric = settingValue;
                setting_locale_numeric = map_locale_numeric.get(settingValue);
                break;
            case "locale_datetime":
                if(value_locale_datetime == settingValue){return;}
                value_locale_datetime = settingValue;
                setting_locale_datetime = map_locale_datetime.get(settingValue);
                break;
            case "time_zone":
                if(value_time_zone == settingValue){return;}
                value_time_zone = settingValue;
                setting_time_zone = map_time_zone.get(settingValue);
                break;
        }

        SharedPreferences settings = context.getSharedPreferences("settings_data", MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(settingName, settingValue);
        editor.apply();
    }

    public static int getSettingValue(String settingName) {
        switch(settingName) {
            case "datetime":
                return value_datetime;
            case "decimal":
                return value_decimal;
            case "price":
                return value_price;
            case "network":
                return value_network;
            case "timeout":
                return value_timeout;
            case "loss":
                return value_loss;
            case "dark":
                return value_dark;
            case "message":
                return value_message;
            case "orientation":
                return value_orientation;
            case "confirm":
                return value_confirm;
            case "asset":
                return value_asset;
            case "max_transactions":
                return value_max_transactions;
            case "locale_numeric":
                return value_locale_numeric;
            case "locale_datetime":
                return value_locale_datetime;
            case "time_zone":
                return value_time_zone;
        }

        return -1;
    }

    public static void loadAllSettings(Context context) {
        // Load all settings into local memory.
        SharedPreferences settings = context.getSharedPreferences("settings_data", MODE_PRIVATE);

        int loaded_datetime = settings.getInt("datetime", 0);
        setting_datetime = map_datetime.get(loaded_datetime);
        value_datetime = loaded_datetime;

        int loaded_decimal = settings.getInt("decimal", 0);
        setting_decimal = map_decimal.get(loaded_decimal);
        value_decimal = loaded_decimal;

        int loaded_price = settings.getInt("price", 0);
        setting_price = map_price.get(loaded_price);
        value_price = loaded_price;

        int loaded_network = settings.getInt("network", 0);
        setting_network = map_network.get(loaded_network);
        value_network = loaded_network;

        int loaded_timeout = settings.getInt("timeout", 0);
        setting_timeout = map_timeout.get(loaded_timeout);
        value_timeout = loaded_timeout;

        int loaded_loss = settings.getInt("loss", 0);
        setting_loss = map_loss.get(loaded_loss);
        value_loss = loaded_loss;

        int loaded_dark = settings.getInt("dark", 0);
        setting_dark = map_dark.get(loaded_dark);
        value_dark = loaded_dark;

        int loaded_message = settings.getInt("message", 0);
        setting_message = map_message.get(loaded_message);
        value_message = loaded_message;

        int loaded_orientation = settings.getInt("orientation", 0);
        setting_orientation = map_orientation.get(loaded_orientation);
        value_orientation = loaded_orientation;

        int loaded_confirm = settings.getInt("confirm", 0);
        setting_confirm = map_confirm.get(loaded_confirm);
        value_confirm = loaded_confirm;

        int loaded_asset = settings.getInt("asset", 0);
        setting_asset = map_asset.get(loaded_asset);
        value_asset = loaded_asset;

        int loaded_max_transactions = settings.getInt("max_transactions", 0);
        setting_max_transactions = map_max_transactions.get(loaded_max_transactions);
        value_max_transactions = loaded_max_transactions;

        int loaded_locale_numeric = settings.getInt("locale_numeric", 0);
        setting_locale_numeric = map_locale_numeric.get(loaded_locale_numeric);
        value_locale_numeric = loaded_locale_numeric;

        int loaded_locale_datetime = settings.getInt("locale_datetime", 0);
        setting_locale_datetime = map_locale_datetime.get(loaded_locale_datetime);
        value_locale_datetime = loaded_locale_datetime;

        int loaded_time_zone = settings.getInt("time_zone", 0);
        setting_time_zone = map_time_zone.get(loaded_time_zone);
        value_time_zone = loaded_time_zone;
    }

    public static void resetAllSettings(Context context) {
        // Change all settings to their default value.
        setSetting(context, "datetime", 0);
        setSetting(context, "decimal", 0);
        setSetting(context, "price", 0);
        setSetting(context, "network", 0);
        setSetting(context, "timeout", 0);
        setSetting(context, "loss", 0);
        setSetting(context, "dark", 0);
        setSetting(context, "message", 0);
        setSetting(context, "orientation", 0);
        setSetting(context, "confirm", 0);
        setSetting(context, "asset", 0);
        setSetting(context, "max_transactions", 0);
        setSetting(context, "locale_numeric", 0);
        setSetting(context, "locale_datetime", 0);
        setSetting(context, "time_zone", 0);
    }
}
