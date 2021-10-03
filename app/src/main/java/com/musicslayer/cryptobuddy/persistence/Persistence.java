package com.musicslayer.cryptobuddy.persistence;

import android.content.Context;

import com.musicslayer.cryptobuddy.util.ExceptionLogger;
import com.musicslayer.cryptobuddy.util.Reflect;

import java.util.ArrayList;
import java.util.HashMap;

// Methods to quickly manipulate all persistent app data on a user's device.
public class Persistence {
    // All classes with persistent data.
    final public static HashMap<String, Class<?>> persistentClassMap;
    static {
        persistentClassMap = new HashMap<>();
        persistentClassMap.put("AddressHistory", AddressHistory.class);
        persistentClassMap.put("AddressPortfolio", AddressPortfolio.class);
        persistentClassMap.put("PrivacyPolicy", PrivacyPolicy.class);
        persistentClassMap.put("Purchases", Purchases.class);
        persistentClassMap.put("Review", Review.class);
        persistentClassMap.put("Settings", Settings.class);
        persistentClassMap.put("TransactionPortfolio", TransactionPortfolio.class);
    }

    public static HashMap<String, HashMap<String, String>> getAllData() {
        // Return a representation of all the persistent data stored in the app.
        // TokenList has too much data so omit that one.
        HashMap<String, HashMap<String, String>> allDataMap = new HashMap<>();

        // Individually, try to add each piece of data.
        ArrayList<String> keys = new ArrayList<>(persistentClassMap.keySet());
        for(String key : keys) {
            try {
                Class<?> value = persistentClassMap.get(key);
                if(value == null) { throw new NullPointerException(); }

                allDataMap.put(key, Reflect.callStaticMethodOrError(value, "getAllData"));
            }
            catch(Exception e) {
                try {
                    ExceptionLogger.processException(e);

                    // Put one special key with error information.
                    HashMap<String, String> errorMap = new HashMap<>();
                    errorMap.put("!ERROR!", ExceptionLogger.getExceptionText(e));
                    allDataMap.put("AddressHistory", errorMap);
                }
                catch(Exception ignored) {
                    // Even the error logging code failed! Just give up at this point.
                }
            }
        }

        return allDataMap;
    }

    public static void resetAllData(Context context) {
        // Resets all stored persistent data in the app. App should be just like a new install.
        // Individually, try to reset each piece of data.
        ArrayList<String> keys = new ArrayList<>(persistentClassMap.keySet());
        for(String key : keys) {
            try {
                Class<?> value = persistentClassMap.get(key);
                if(value == null) { throw new NullPointerException(); }

                Reflect.callResetAllData(value, context);
            }
            catch(Exception e) {
                ExceptionLogger.processException(e);
            }
        }
    }
}
