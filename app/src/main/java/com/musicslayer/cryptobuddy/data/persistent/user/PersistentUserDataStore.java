package com.musicslayer.cryptobuddy.data.persistent.user;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.data.bridge.DataBridge;
import com.musicslayer.cryptobuddy.util.FileUtil;
import com.musicslayer.cryptobuddy.util.SharedPreferencesUtil;
import com.musicslayer.cryptobuddy.util.ThrowableUtil;
import com.musicslayer.cryptobuddy.util.ReflectUtil;

import java.util.ArrayList;
import java.util.HashMap;

// Methods to quickly manipulate all persistent app data on a user's device.
abstract public class PersistentUserDataStore {
    public static ArrayList<PersistentUserDataStore> persistent_user_data_stores;
    public static HashMap<String, PersistentUserDataStore> persistent_user_data_store_map;
    public static ArrayList<String> persistent_user_data_store_names;

    public static void initialize() {
        persistent_user_data_store_names = FileUtil.readFileIntoLines(R.raw.data_persistent_user);

        persistent_user_data_stores = new ArrayList<>();
        persistent_user_data_store_map = new HashMap<>();

        for(String persistentUserDataStoreName : persistent_user_data_store_names) {
            PersistentUserDataStore persistentUserDataStore = ReflectUtil.constructClassInstanceFromName("com.musicslayer.cryptobuddy.data.persistent.user." + persistentUserDataStoreName);
            persistent_user_data_stores.add(persistentUserDataStore);
            persistent_user_data_store_map.put(persistentUserDataStoreName, persistentUserDataStore);
        }
    }

    // For now, just use the name as the key.
    public String getKey() {
        return getName();
    }

    abstract public String getName();

    abstract public boolean canExport();
    abstract public String doExport();
    abstract public void doImport(String s);

    abstract public String getSharedPreferencesKey();

    abstract public void saveAllData(); // Write all local data to stored data.
    abstract public void loadAllData(); // Read all stored data into local data.
    abstract public void resetAllData(); // Erase all stored and local data.

    public static <T extends PersistentUserDataStore> T getInstance(Class<T> clazz) {
        // Subclasses have many unique methods, so use this to cast the instance.
        for(PersistentUserDataStore persistentUserDataStore : persistent_user_data_stores) {
            if(clazz.isInstance(persistentUserDataStore)) {
                return clazz.cast(persistentUserDataStore);
            }
        }

        // Do not use null or UnknownPersistentDataStore here. The input class must exist.
        throw new IllegalStateException();
    }

    public static ArrayList<String> getAllExportableDataTypes() {
        // Return all the possible data types that we can export.
        ArrayList<String> dataTypes = new ArrayList<>();

        for(PersistentUserDataStore persistentUserDataStore : persistent_user_data_stores) {
            if(persistentUserDataStore.canExport()) {
                dataTypes.add(persistentUserDataStore.getSharedPreferencesKey());
            }
        }

        return dataTypes;
    }

    public static String exportStoredDataToJSON(ArrayList<String> dataTypes) {
        // Export a JSON representation of persistent data stored in the app.

        // Each SharedPreferences key maps to its data.
        DataBridge.JSONObjectDataBridge o = new DataBridge.JSONObjectDataBridge();

        // Individually, try to export each piece of data.
        for(PersistentUserDataStore persistentUserDataStore : persistent_user_data_stores) {
            try {
                String key = persistentUserDataStore.getSharedPreferencesKey();
                if(!dataTypes.contains(key)) { continue; }

                String value = persistentUserDataStore.doExport();
                o.serialize(key, value, String.class);
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
        DataBridge.JSONObjectDataBridge o;
        try {
            o = new DataBridge.JSONObjectDataBridge(json);
        }
        catch(Exception e) {
            throw new IllegalStateException(e);
        }

        // Individually, try to import each piece of data.
        for(PersistentUserDataStore persistentUserDataStore : persistent_user_data_stores) {
            try {
                String key = persistentUserDataStore.getSharedPreferencesKey();
                if(!o.has(key) || !dataTypes.contains(key)) { continue; }

                String value = o.deserialize(key, String.class);
                persistentUserDataStore.doImport(value);
            }
            catch(Exception e) {
                // If one class's data cannot be imported, skip it and do nothing.
                ThrowableUtil.processThrowable(e);
            }
        }
    }

    public static HashMap<String, HashMap<String, String>> getAllStoredData() {
        // Return a representation of all the persistent data stored in the app.
        HashMap<String, HashMap<String, String>> allDataMap = new HashMap<>();

        // Individually, try to add each piece of data.
        for(PersistentUserDataStore persistentUserDataStore : persistent_user_data_stores) {
            try {
                String key = persistentUserDataStore.getSharedPreferencesKey();
                HashMap<String, String> value = SharedPreferencesUtil.getDataMap(key);
                allDataMap.put(key, value);
            }
            catch(Exception e) {
                ThrowableUtil.processThrowable(e);

                try {
                    String clazzString = persistentUserDataStore.getClass().getSimpleName();

                    // Put a default entry in here, and then try to replace with error information.
                    // If this code errors, then just give up!
                    HashMap<String, String> noInfoMap = new HashMap<>();
                    noInfoMap.put("!ERROR!", "!NO_INFO!");
                    allDataMap.put(clazzString, noInfoMap);

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

    public static void saveAllStoredData() {
        for(PersistentUserDataStore persistentUserDataStore : persistent_user_data_stores) {
            try {
                persistentUserDataStore.saveAllData();
            }
            catch(Exception e) {
                ThrowableUtil.processThrowable(e);
            }
        }
    }

    public static void loadAllStoredData() {
        for(PersistentUserDataStore persistentUserDataStore : persistent_user_data_stores) {
            try {
                persistentUserDataStore.loadAllData();
            }
            catch(Exception e) {
                ThrowableUtil.processThrowable(e);
            }
        }
    }

    public static boolean resetAllStoredData() {
        // Resets all stored persistent data in the app. App should be just like a new install.
        boolean isComplete = true;

        for(PersistentUserDataStore persistentUserDataStore : persistent_user_data_stores) {
            try {
                persistentUserDataStore.resetAllData();
            }
            catch(Exception e) {
                ThrowableUtil.processThrowable(e);
                isComplete = false;
            }
        }

        return isComplete;
    }
}
