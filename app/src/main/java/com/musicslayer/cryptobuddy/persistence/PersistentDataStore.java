package com.musicslayer.cryptobuddy.persistence;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.data.Exportation;
import com.musicslayer.cryptobuddy.json.JSONWithNull;
import com.musicslayer.cryptobuddy.util.FileUtil;
import com.musicslayer.cryptobuddy.util.SharedPreferencesUtil;
import com.musicslayer.cryptobuddy.util.ThrowableUtil;
import com.musicslayer.cryptobuddy.util.ReflectUtil;

import java.util.ArrayList;
import java.util.HashMap;

// Methods to quickly manipulate all persistent app data on a user's device.
abstract public class PersistentDataStore {
    public static ArrayList<PersistentDataStore> persistent_data_stores;
    public static HashMap<String, PersistentDataStore> persistent_data_store_map;
    public static ArrayList<String> persistent_data_store_names;

    public static void initialize() {
        persistent_data_store_names = FileUtil.readFileIntoLines(R.raw.persistent_data_store);

        persistent_data_stores = new ArrayList<>();
        persistent_data_store_map = new HashMap<>();

        for(String persistentDataStoreName : persistent_data_store_names) {
            PersistentDataStore persistentDataStore = ReflectUtil.constructClassInstanceFromName("com.musicslayer.cryptobuddy.persistence." + persistentDataStoreName);
            persistent_data_stores.add(persistentDataStore);
            persistent_data_store_map.put(persistentDataStoreName, persistentDataStore);
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
    abstract public void resetAllData(); // Erase all stored and local data.

    @SuppressWarnings("unchecked")
    public static <T extends PersistentDataStore> T getInstance(Class<T> clazz) {
        // Subclasses have many unique methods, so use this to cast the instance.
        PersistentDataStore instance = UnknownPersistentDataStore.createUnknownPersistentDataStore(null);

        for(PersistentDataStore persistentDataStore : persistent_data_stores) {
            if(clazz.isInstance(persistentDataStore)) {
                instance = persistentDataStore;
            }
        }

        return (T)instance;
    }

    public static ArrayList<String> getAllExportableDataTypes() {
        // Return all the possible data types that we can export.
        ArrayList<String> dataTypes = new ArrayList<>();

        for(PersistentDataStore persistentDataStore : persistent_data_stores) {
            if(persistentDataStore.canExport()) {
                dataTypes.add(persistentDataStore.getSharedPreferencesKey());
            }
        }

        return dataTypes;
    }

    public static String exportStoredDataToJSON(ArrayList<String> dataTypes) {
        // Export a JSON representation of persistent data stored in the app.

        // Each SharedPreferences key maps to its data.
        JSONWithNull.JSONObjectWithNull o = new JSONWithNull.JSONObjectWithNull();

        // Individually, try to export each piece of data.
        for(PersistentDataStore persistentDataStore : persistent_data_stores) {
            try {
                String key = persistentDataStore.getSharedPreferencesKey();
                if(!dataTypes.contains(key)) { continue; }

                String value = persistentDataStore.doExport();
                o.put(key, value, String.class);
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
        for(PersistentDataStore persistentDataStore : persistent_data_stores) {
            try {
                String key = persistentDataStore.getSharedPreferencesKey();
                if(!o.has(key) || !dataTypes.contains(key)) { continue; }

                String value = o.get(key, String.class);
                persistentDataStore.doImport(value);
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
        for(PersistentDataStore persistentDataStore : persistent_data_stores) {
            try {
                String key = persistentDataStore.getSharedPreferencesKey();
                HashMap<String, String> value = SharedPreferencesUtil.getDataMap(key);
                allDataMap.put(key, value);
            }
            catch(Exception e) {
                ThrowableUtil.processThrowable(e);

                try {
                    String clazzString = persistentDataStore.getClass().getSimpleName();

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

    public static boolean resetAllStoredData() {
        // Resets all stored persistent data in the app. App should be just like a new install.
        boolean isComplete = true;

        for(PersistentDataStore persistentDataStore : persistent_data_stores) {
            try {
                // This method should erase both active and stored data.
                persistentDataStore.resetAllData();
            }
            catch(Exception e) {
                ThrowableUtil.processThrowable(e);
                isComplete = false;
            }
        }

        return isComplete;
    }
}
