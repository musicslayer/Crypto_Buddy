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

    public String getSharedPreferencesKey() { return null; }
    public void resetAllData() {}
    public boolean canExport() { return false; }

    public static UnknownPersistentDataStore createUnknownPersistentDataStore(String key) {
        return new UnknownPersistentDataStore(key);
    }

    private UnknownPersistentDataStore(String key) {
        this.key = key;
    }
}
