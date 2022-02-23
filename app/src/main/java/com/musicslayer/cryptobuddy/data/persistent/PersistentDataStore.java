package com.musicslayer.cryptobuddy.data.persistent;

import com.musicslayer.cryptobuddy.data.persistent.app.PersistentAppDataStore;
import com.musicslayer.cryptobuddy.data.persistent.user.PersistentUserDataStore;
import com.musicslayer.cryptobuddy.json.JSONWithNull;
import com.musicslayer.cryptobuddy.util.ThrowableUtil;

import java.util.ArrayList;

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
        JSONWithNull.JSONObjectWithNull o = new JSONWithNull.JSONObjectWithNull();

        // Individually, try to export each piece of data.
        for(PersistentAppDataStore persistentAppDataStore : PersistentAppDataStore.persistent_app_data_stores) {
            try {
                String key = persistentAppDataStore.getSharedPreferencesKey();
                if(!dataTypes.contains(key)) { continue; }

                String value = persistentAppDataStore.doExport();
                o.putString(key, value);
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
                o.putString(key, value);
            }
            catch(Exception e) {
                // If one class's data cannot be exported, skip it and do nothing.
                ThrowableUtil.processThrowable(e);
            }
        }

        return o.toStringOrNull();
    }

    public static void importStoredDataFromJSON(ArrayList<String> dataTypes, String json) {
        // Import a JSON representation of persistent data into the app.

        // Each SharedPreferences key maps to its data.
        JSONWithNull.JSONObjectWithNull o;
        try {
            o = new JSONWithNull.JSONObjectWithNull(json);
        }
        catch(Exception e) {
            throw new IllegalStateException(e);
        }

        // Individually, try to import each piece of data.
        for(PersistentAppDataStore persistentAppDataStore : PersistentAppDataStore.persistent_app_data_stores) {
            try {
                String key = persistentAppDataStore.getSharedPreferencesKey();
                if(!o.has(key) || !dataTypes.contains(key)) { continue; }

                String value = o.getString(key);
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
                if(!o.has(key) || !dataTypes.contains(key)) { continue; }

                String value = o.getString(key);
                persistentUserDataStore.doImport(value);
            }
            catch(Exception e) {
                // If one class's data cannot be imported, skip it and do nothing.
                ThrowableUtil.processThrowable(e);
            }
        }
    }
}
