package com.musicslayer.cryptobuddy.persistence;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

import com.musicslayer.cryptobuddy.serialize.Serialization;
import com.musicslayer.cryptobuddy.util.ThrowableUtil;
import com.musicslayer.cryptobuddy.util.ReflectUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

// Methods to quickly manipulate all persistent app data on a user's device.
public class Persistence {
    // All classes with persistent data.
    final public static HashMap<String, Class<?>> persistentClassMap;
    static {
        persistentClassMap = new HashMap<>();
        persistentClassMap.put("AddressHistory", AddressHistory.class);
        persistentClassMap.put("AddressPortfolio", AddressPortfolio.class);
        persistentClassMap.put("CoinManagerList", CoinManagerList.class);
        persistentClassMap.put("ExchangePortfolio", ExchangePortfolio.class);
        persistentClassMap.put("FiatManagerList", FiatManagerList.class);
        persistentClassMap.put("Policy", Policy.class);
        persistentClassMap.put("Purchases", Purchases.class);
        persistentClassMap.put("Review", Review.class);
        persistentClassMap.put("SettingList", SettingList.class);
        persistentClassMap.put("TokenManagerList", TokenManagerList.class);
        persistentClassMap.put("TransactionPortfolio", TransactionPortfolio.class);
    }

    public static String exportAllToJSON() {
        // Return a JSON representation of all the persistent data stored in the app.

        // Map each SharedPreferences key to it's data.
        Serialization.JSONObjectWithNull o = new Serialization.JSONObjectWithNull();

        // Individually, try to add each piece of data.
        for(String clazzString : new ArrayList<>(persistentClassMap.keySet())) {
            try {
                Class<?> clazz = persistentClassMap.get(clazzString);
                if(clazz == null) { throw new NullPointerException(); }

                String key = ReflectUtil.callStaticMethod(clazz, "getSharedPreferencesKey");
                String value = ReflectUtil.callStaticMethod(clazz, "exportToJSON");

                o.put(key, value);
            }
            catch(Exception e) {
                // If one class's data cannot be written, skip it and do nothing.
                ThrowableUtil.processThrowable(e);
            }
        }

        return o.toStringOrNull();
    }

    public static void importAllFromJSON(Context context, String json){

        // Map each SharedPreferences key to it's data.
        Serialization.JSONObjectWithNull o;
        try {
            o = new Serialization.JSONObjectWithNull(json);
        }
        catch(Exception ignored) {
            return;
        }

        // Individually, try to add each piece of data.
        for(String clazzString : new ArrayList<>(persistentClassMap.keySet())) {
            try {
                Class<?> clazz = persistentClassMap.get(clazzString);
                if(clazz == null) { throw new NullPointerException(); }

                String key = ReflectUtil.callStaticMethod(clazz, "getSharedPreferencesKey");
                String value = o.getString(key);

                //ReflectUtil.callStaticMethod(clazz, "getSharedPreferencesKey", value);
                if(key.equals("settings_data")) {
                    SettingList.importFromJSON1(context, value);
                }

                //ReflectUtil.callStaticMethod(clazz, "exportToJSON");

                //o.put(key, value);
            }
            catch(Exception e) {
                // If one class's data cannot be imported, skip it and do nothing.
                ThrowableUtil.processThrowable(e);
            }
        }
    }

    public static HashMap<String, HashMap<String, String>> getAllData(Context context) {
        // Return a representation of all the persistent data stored in the app.
        HashMap<String, HashMap<String, String>> allDataMap = new HashMap<>();

        // Individually, try to add each piece of data.
        for(String clazzString : new ArrayList<>(persistentClassMap.keySet())) {
            try {
                Class<?> clazz = persistentClassMap.get(clazzString);
                if(clazz == null) { throw new NullPointerException(); }

                String key = ReflectUtil.callStaticMethod(clazz, "getSharedPreferencesKey");
                HashMap<String, String> value = getDataMap(context, key);

                allDataMap.put(key, value);
            }
            catch(Exception e) {
                try {
                    // Put a default entry in here, and then try to replace with error information.
                    // If this code errors, then just give up!
                    HashMap<String, String> noInfoMap = new HashMap<>();
                    noInfoMap.put("!ERROR!", "!NO_INFO!");
                    allDataMap.put(clazzString, noInfoMap);

                    ThrowableUtil.processThrowable(e);

                    HashMap<String, String> errorMap = new HashMap<>();
                    errorMap.put("!ERROR!", ThrowableUtil.getThrowableText(e));
                    allDataMap.put(clazzString, errorMap);
                }
                catch(Exception ignored) {
                }
            }
        }

        return allDataMap;
    }

    public static HashMap<String, String> getDataMap(Context context, String name) {
        // Get all data inside a SharedPreferences instance as a string.
        HashMap<String, String> hashMap = new HashMap<>();

        SharedPreferences settings = context.getSharedPreferences(name, MODE_PRIVATE);
        Map<String, ?> settingsMap = settings.getAll();
        for(String key : settingsMap.keySet()) {
            Object value = settingsMap.get(key);
            String valueString = value == null ? "null" : value.toString();
            hashMap.put(key, valueString);
        }

        return hashMap;
    }

    public static boolean resetAllData(Context context) {
        // Resets all stored persistent data in the app. App should be just like a new install.
        // Individually, try to reset each piece of data.
        // Note that each "resetData" method should erase both active and stored data.
        boolean isComplete = true;

        ArrayList<String> keys = new ArrayList<>(persistentClassMap.keySet());
        for(String key : keys) {
            try {
                Class<?> value = persistentClassMap.get(key);
                if(value == null) { throw new NullPointerException(); }

                ReflectUtil.callResetAllData(value, context);
            }
            catch(Exception e) {
                ThrowableUtil.processThrowable(e);
                isComplete = false;
            }
        }

        return isComplete;
    }
}
