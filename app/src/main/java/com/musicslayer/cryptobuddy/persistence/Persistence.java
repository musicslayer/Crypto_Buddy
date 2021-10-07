package com.musicslayer.cryptobuddy.persistence;

import android.content.Context;

import com.musicslayer.cryptobuddy.util.ThrowableUtil;
import com.musicslayer.cryptobuddy.util.ReflectUtil;

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
        persistentClassMap.put("TokenList", TokenManagerList.class);
        persistentClassMap.put("TransactionPortfolio", TransactionPortfolio.class);
    }

    public static HashMap<String, HashMap<String, String>> getAllData() {
        // Return a representation of all the persistent data stored in the app.
        HashMap<String, HashMap<String, String>> allDataMap = new HashMap<>();

        // Individually, try to add each piece of data.
        ArrayList<String> keys = new ArrayList<>(persistentClassMap.keySet());
        for(String key : keys) {
            try {
                Class<?> value = persistentClassMap.get(key);
                if(value == null) { throw new NullPointerException(); }

                allDataMap.put(key, ReflectUtil.callStaticMethod(value, "getAllData"));
            }
            catch(Exception e) {
                try {
                    // Put a default entry in here, and then try to replace with error information.
                    // If this code errors, then just give up!
                    HashMap<String, String> noInfoMap = new HashMap<>();
                    noInfoMap.put("!ERROR!", "!NO_INFO!");
                    allDataMap.put(key, noInfoMap);

                    ThrowableUtil.processThrowable(e);

                    HashMap<String, String> errorMap = new HashMap<>();
                    errorMap.put("!ERROR!", ThrowableUtil.getThrowableText(e));
                    allDataMap.put(key, errorMap);
                }
                catch(Exception ignored) {
                }
            }
        }

        return allDataMap;
    }

    public static void resetAllData(Context context) {
        // Resets all stored persistent data in the app. App should be just like a new install.
        // Individually, try to reset each piece of data.
        // Note that each "resetData" method should erase both active and stored data.
        ArrayList<String> keys = new ArrayList<>(persistentClassMap.keySet());
        for(String key : keys) {
            try {
                Class<?> value = persistentClassMap.get(key);
                if(value == null) { throw new NullPointerException(); }

                ReflectUtil.callResetAllData(value, context);
            }
            catch(Exception e) {
                ThrowableUtil.processThrowable(e);
            }
        }
    }
}
