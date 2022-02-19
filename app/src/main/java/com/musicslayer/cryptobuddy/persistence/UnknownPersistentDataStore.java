package com.musicslayer.cryptobuddy.persistence;

public class UnknownPersistentDataStore extends PersistentDataStore {
    String key;

    public String getKey() { return key; }

    public String getName() {
        if(key == null) {
            return "?UNKNOWN_PERSISTENT_DATA_STORE?";
        }
        else {
            return "?UNKNOWN_PERSISTENT_DATA_STORE (" + key + ")?";
        }
    }

    public boolean canExport() { return false; }
    public String doExport() { return null; }
    public void doImport(String s) {}

    public String getSharedPreferencesKey() { return null; }
    public void resetAllData() {}

    public static UnknownPersistentDataStore createUnknownPersistentDataStore(String key) {
        return new UnknownPersistentDataStore(key);
    }

    private UnknownPersistentDataStore(String key) {
        this.key = key;
    }
}
