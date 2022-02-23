package com.musicslayer.cryptobuddy.data.persistent;

import com.musicslayer.cryptobuddy.data.bridge.DataBridge;
import com.musicslayer.cryptobuddy.data.persistent.app.PersistentAppDataStore;
import com.musicslayer.cryptobuddy.data.persistent.user.PersistentUserDataStore;
import com.musicslayer.cryptobuddy.util.HashMapUtil;
import com.musicslayer.cryptobuddy.util.ThrowableUtil;

import java.util.ArrayList;
import java.util.HashMap;

// For now, app and user data is not separated everywhere, so use this class to handle both.
abstract public class PersistentDataStore {
    public static ArrayList<String> getAllVisibleDataTypes() {
        // Return all the possible data types that we can export.
        ArrayList<String> dataTypes = new ArrayList<>();

        for(PersistentAppDataStore persistentAppDataStore : PersistentAppDataStore.persistent_app_data_stores) {
            if(persistentAppDataStore.isVisible()) {
                dataTypes.add(persistentAppDataStore.getSharedPreferencesKey());
            }
        }

        for(PersistentUserDataStore persistentUserDataStore : PersistentUserDataStore.persistent_user_data_stores) {
            if(persistentUserDataStore.isVisible()) {
                dataTypes.add(persistentUserDataStore.getSharedPreferencesKey());
            }
        }

        return dataTypes;
    }

    public static String exportStoredDataToJSON(ArrayList<String> dataTypes) {
        // Export a JSON representation of persistent data stored in the app.

        // Each SharedPreferences key maps to its data.
        HashMap<String, String> data = new HashMap<>();

        // Individually, try to export each piece of data.
        for(PersistentAppDataStore persistentAppDataStore : PersistentAppDataStore.persistent_app_data_stores) {
            try {
                String key = persistentAppDataStore.getSharedPreferencesKey();
                if(!dataTypes.contains(key)) { continue; }

                String value = persistentAppDataStore.doExport();
                HashMapUtil.putValueInMap(data, key, value);
            }
            catch(Exception e) {
                // If one class's data cannot be exported, skip it and do nothing.
                ThrowableUtil.processThrowable(e);
            }
        }

        for(PersistentUserDataStore persistentUserDataStore : PersistentUserDataStore.persistent_user_data_stores) {
            try {
                String key = persistentUserDataStore.getSharedPreferencesKey();
                if(!dataTypes.contains(key)) { continue; }

                String value = persistentUserDataStore.doExport();
                HashMapUtil.putValueInMap(data, key, value);
            }
            catch(Exception e) {
                // If one class's data cannot be exported, skip it and do nothing.
                ThrowableUtil.processThrowable(e);
            }
        }

        return DataBridge.serializeHashMap(data, String.class, String.class);
    }

    public static void importStoredDataFromJSON(ArrayList<String> dataTypes, String json) {
        // Import a JSON representation of persistent data into the app.

        // Each SharedPreferences key maps to its data.
        HashMap<String, String> data = DataBridge.deserializeHashMap(json, String.class, String.class);
        if(data == null) { return; }

        // Individually, try to import each piece of data.
        for(PersistentAppDataStore persistentAppDataStore : PersistentAppDataStore.persistent_app_data_stores) {
            try {
                String key = persistentAppDataStore.getSharedPreferencesKey();
                if(!data.containsKey(key) || !dataTypes.contains(key)) { continue; }

                String value = HashMapUtil.getValueFromMap(data, key);
                persistentAppDataStore.doImport(value);
            }
            catch(Exception e) {
                // If one class's data cannot be imported, skip it and do nothing.
                ThrowableUtil.processThrowable(e);
            }
        }

        for(PersistentUserDataStore persistentUserDataStore : PersistentUserDataStore.persistent_user_data_stores) {
            try {
                String key = persistentUserDataStore.getSharedPreferencesKey();
                if(!data.containsKey(key) || !dataTypes.contains(key)) { continue; }

                String value = HashMapUtil.getValueFromMap(data, key);
                persistentUserDataStore.doImport(value);
            }
            catch(Exception e) {
                // If one class's data cannot be imported, skip it and do nothing.
                ThrowableUtil.processThrowable(e);
            }
        }
    }
}
