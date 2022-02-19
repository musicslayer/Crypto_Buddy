package com.musicslayer.cryptobuddy.persistence;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

import com.musicslayer.cryptobuddy.data.Exportation;
import com.musicslayer.cryptobuddy.json.JSONWithNull;
import com.musicslayer.cryptobuddy.util.ThrowableUtil;
import com.musicslayer.cryptobuddy.util.ReflectUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

// TODO Update Proguard rules for export/import functions.

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

    public static ArrayList<String> getAllDataTypes() {
        // Return all the possible data types that we can export (in alphabetical order).
        ArrayList<String> dataTypes = new ArrayList<>();

        ArrayList<String> sortedKeys = new ArrayList<>(persistentClassMap.keySet());
        Collections.sort(sortedKeys);

        for(String clazzString : sortedKeys) {
            Class<?> clazz = persistentClassMap.get(clazzString);
            if(clazz == null) { throw new NullPointerException(); }

            boolean canExport = ReflectUtil.callStaticMethod(clazz, "canExport");
            if(!canExport) { continue; }

            String key = ReflectUtil.callStaticMethod(clazz, "getSharedPreferencesKey");
            dataTypes.add(key);
        }

        return dataTypes;
    }

    public static String exportAllToJSON(Context context, ArrayList<String> dataTypes) {
        // Return a JSON representation of all the persistent data stored in the app.

        // Each SharedPreferences key maps to its data.
        JSONWithNull.JSONObjectWithNull o = new JSONWithNull.JSONObjectWithNull();

        // Individually, try to export each piece of data (in alphabetical order).
        ArrayList<String> sortedKeys = new ArrayList<>(persistentClassMap.keySet());
        Collections.sort(sortedKeys);

        for(String clazzString : sortedKeys) {
            try {
                Class<?> clazz = persistentClassMap.get(clazzString);
                if(clazz == null) { throw new NullPointerException(); }

                String key = ReflectUtil.callStaticMethod(clazz, "getSharedPreferencesKey");
                if(!dataTypes.contains(key)) { continue; }

                String value = Exportation.exportData(clazz);
                o.put(key, value, String.class);
            }
            catch(Exception e) {
                // If one class's data cannot be exported, skip it and do nothing.
                ThrowableUtil.processThrowable(e);
            }
        }

        return o.toStringOrNull();
    }

    public static void importAllFromJSON(Context context, ArrayList<String> dataTypes, String json) {
        // Each SharedPreferences key maps to its data.
        JSONWithNull.JSONObjectWithNull o;
        try {
            o = new JSONWithNull.JSONObjectWithNull(json);
        }
        catch(Exception e) {
            throw new IllegalStateException(e);
        }

        // Individually, try to import each piece of data (in alphabetical order).
        ArrayList<String> sortedKeys = new ArrayList<>(persistentClassMap.keySet());
        Collections.sort(sortedKeys);

        for(String clazzString : sortedKeys) {
            try {
                Class<?> clazz = persistentClassMap.get(clazzString);
                if(clazz == null) { throw new NullPointerException(); }

                String key = ReflectUtil.callStaticMethod(clazz, "getSharedPreferencesKey");
                if(!o.has(key) || !dataTypes.contains(key)) { continue; }

                String value = o.get(key, String.class);
                Exportation.importData(value, clazz);
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

        // Individually, try to add each piece of data (in alphabetical order).
        ArrayList<String> sortedKeys = new ArrayList<>(persistentClassMap.keySet());
        Collections.sort(sortedKeys);

        for(String clazzString : sortedKeys) {
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
        // Get all data inside a SharedPreferences instance as a HashMap.
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
        // Individually, try to reset each piece of data (in alphabetical order).
        // Note that each "resetData" method should erase both active and stored data.
        boolean isComplete = true;

        ArrayList<String> sortedKeys = new ArrayList<>(persistentClassMap.keySet());
        Collections.sort(sortedKeys);

        for(String clazzString : sortedKeys) {
            try {
                Class<?> value = persistentClassMap.get(clazzString);
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
