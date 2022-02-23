package com.musicslayer.cryptobuddy.data.persistent.app;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.data.bridge.LegacyDataBridge;
import com.musicslayer.cryptobuddy.util.FileUtil;
import com.musicslayer.cryptobuddy.util.ReflectUtil;
import com.musicslayer.cryptobuddy.util.SharedPreferencesUtil;
import com.musicslayer.cryptobuddy.util.ThrowableUtil;

import java.util.ArrayList;
import java.util.HashMap;

// Methods to quickly manipulate all persistent app data on a user's device.
abstract public class PersistentAppDataStore {
    public static ArrayList<PersistentAppDataStore> persistent_app_data_stores;
    public static HashMap<String, PersistentAppDataStore> persistent_app_data_store_map;
    public static ArrayList<String> persistent_app_data_store_names;

    public static void initialize() {
        persistent_app_data_store_names = FileUtil.readFileIntoLines(R.raw.data_persistent_app);

        persistent_app_data_stores = new ArrayList<>();
        persistent_app_data_store_map = new HashMap<>();

        for(String persistentAppDataStoreName : persistent_app_data_store_names) {
            PersistentAppDataStore persistentAppDataStore = ReflectUtil.constructClassInstanceFromName("com.musicslayer.cryptobuddy.data.persistent.app." + persistentAppDataStoreName);
            persistent_app_data_stores.add(persistentAppDataStore);
            persistent_app_data_store_map.put(persistentAppDataStoreName, persistentAppDataStore);
        }
    }

    // For now, just use the name as the key.
    public String getKey() {
        return getName();
    }

    abstract public String getName();

    abstract public boolean isVisible(); // Whether app data can be seen by user, exported, etc...
    abstract public String doExport();
    abstract public void doImport(String s);

    abstract public String getSharedPreferencesKey();

    abstract public void saveAllData(); // Write all local data to stored data.
    abstract public void loadAllData(); // Read all stored data into local data.
    abstract public void resetAllData(); // Erase all stored and local data.

    public static <T extends PersistentAppDataStore> T getInstance(Class<T> clazz) {
        // Subclasses have many unique methods, so use this to cast the instance.
        for(PersistentAppDataStore persistentAppDataStore : persistent_app_data_stores) {
            if(clazz.isInstance(persistentAppDataStore)) {
                return clazz.cast(persistentAppDataStore);
            }
        }

        // Do not use null or an Unknown object here. The input class must exist.
        throw new IllegalStateException();
    }

    public static ArrayList<String> getAllVisibleDataTypes() {
        // Return all the possible data types that we can export.
        ArrayList<String> dataTypes = new ArrayList<>();

        for(PersistentAppDataStore persistentAppDataStore : persistent_app_data_stores) {
            if(persistentAppDataStore.isVisible()) {
                dataTypes.add(persistentAppDataStore.getSharedPreferencesKey());
            }
        }

        return dataTypes;
    }

    public static String exportStoredDataToJSON(ArrayList<String> dataTypes) {
        // Export a JSON representation of persistent data stored in the app.

        // Each SharedPreferences key maps to its data.
        LegacyDataBridge.JSONObjectDataBridge o = new LegacyDataBridge.JSONObjectDataBridge();

        // Individually, try to export each piece of data.
        for(PersistentAppDataStore persistentAppDataStore : persistent_app_data_stores) {
            try {
                String key = persistentAppDataStore.getSharedPreferencesKey();
                if(!dataTypes.contains(key)) { continue; }

                String value = persistentAppDataStore.doExport();
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
        LegacyDataBridge.JSONObjectDataBridge o;
        try {
            o = new LegacyDataBridge.JSONObjectDataBridge(json);
        }
        catch(Exception e) {
            throw new IllegalStateException(e);
        }

        // Individually, try to import each piece of data.
        for(PersistentAppDataStore persistentAppDataStore : persistent_app_data_stores) {
            try {
                String key = persistentAppDataStore.getSharedPreferencesKey();
                if(!o.has(key) || !dataTypes.contains(key)) { continue; }

                String value = o.deserialize(key, String.class);
                persistentAppDataStore.doImport(value);
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
        for(PersistentAppDataStore persistentAppDataStore : persistent_app_data_stores) {
            try {
                String key = persistentAppDataStore.getSharedPreferencesKey();
                HashMap<String, String> value = SharedPreferencesUtil.getDataMap(key);
                allDataMap.put(key, value);
            }
            catch(Exception e) {
                ThrowableUtil.processThrowable(e);

                try {
                    String clazzString = persistentAppDataStore.getClass().getSimpleName();

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
        for(PersistentAppDataStore persistentAppDataStore : persistent_app_data_stores) {
            try {
                persistentAppDataStore.saveAllData();
            }
            catch(Exception e) {
                ThrowableUtil.processThrowable(e);
            }
        }
    }

    public static void loadAllStoredData() {
        for(PersistentAppDataStore persistentAppDataStore : persistent_app_data_stores) {
            try {
                persistentAppDataStore.loadAllData();
            }
            catch(Exception e) {
                ThrowableUtil.processThrowable(e);
            }
        }
    }

    public static boolean resetAllStoredData(ArrayList<String> dataTypes) {
        // Resets all stored persistent data in the app that matches the dataTypes.
        boolean isComplete = true;

        for(PersistentAppDataStore persistentAppDataStore : persistent_app_data_stores) {
            try {
                String key = persistentAppDataStore.getSharedPreferencesKey();
                if(!dataTypes.contains(key)) { continue; }

                persistentAppDataStore.resetAllData();
            }
            catch(Exception e) {
                ThrowableUtil.processThrowable(e);
                isComplete = false;
            }
        }

        return isComplete;
    }

    public static boolean resetAllStoredData() {
        // Resets all stored persistent data in the app. App should be just like a new install.
        boolean isComplete = true;

        for(PersistentAppDataStore persistentAppDataStore : persistent_app_data_stores) {
            try {
                persistentAppDataStore.resetAllData();
            }
            catch(Exception e) {
                ThrowableUtil.processThrowable(e);
                isComplete = false;
            }
        }

        return isComplete;
    }
}
