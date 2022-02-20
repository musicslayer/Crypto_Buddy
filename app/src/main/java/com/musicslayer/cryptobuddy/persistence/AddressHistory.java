package com.musicslayer.cryptobuddy.persistence;

import android.content.SharedPreferences;

import java.util.ArrayList;

import com.musicslayer.cryptobuddy.api.address.CryptoAddress;
import com.musicslayer.cryptobuddy.data.DataBridge;
import com.musicslayer.cryptobuddy.data.Exportation;
import com.musicslayer.cryptobuddy.data.Serialization;
import com.musicslayer.cryptobuddy.util.SharedPreferencesUtil;

public class AddressHistory extends PersistentDataStore implements Exportation.ExportableToJSON, Exportation.Versionable {
    public String getName() { return "AddressHistory"; }

    public boolean canExport() { return true; }
    public String doExport() { return Exportation.exportData(this, AddressHistory.class); }
    public void doImport(String s) { Exportation.importData(this, s, AddressHistory.class); }

    // This default will cause an error when deserialized. We should never see this value used.
    public final static String DEFAULT = "null";
    final public static int HISTORY_LIMIT = 10;

    public static ArrayList<AddressHistoryObj> settings_address_history = new ArrayList<>();

    public String getSharedPreferencesKey() {
        return "address_history_data";
    }

    public static boolean isSaved(AddressHistoryObj addressHistoryObj) {
        return settings_address_history.contains(addressHistoryObj);
    }

    public static AddressHistoryObj getFromCryptoAddress(CryptoAddress cryptoAddress) {
        for(AddressHistoryObj h : settings_address_history) {
            if(cryptoAddress.equals(h.cryptoAddress)) {
                return h;
            }
        }
        return null;
    }

    public void addAddressToHistory(AddressHistoryObj addressHistoryObj) {
        if(isSaved(addressHistoryObj)) {
            settings_address_history.remove(addressHistoryObj);
            settings_address_history.add(0, addressHistoryObj);
        }
        else if(settings_address_history.size() == AddressHistory.HISTORY_LIMIT) {
            settings_address_history.remove(AddressHistory.HISTORY_LIMIT - 1);
            settings_address_history.add(0, addressHistoryObj);
        }
        else {
            settings_address_history.add(0, addressHistoryObj);
        }

        saveAllData();
    }

    public void removeAddressFromHistory(AddressHistoryObj addressHistoryObj) {
        settings_address_history.remove(addressHistoryObj);
        saveAllData();
    }

    public void saveAllData() {
        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.clear();

        int size = settings_address_history.size();
        editor.putInt("address_history_size", size);

        for(int i = 0; i < size; i++) {
            AddressHistoryObj addressHistoryObj = settings_address_history.get(i);
            editor.putString("address_history" + i, Serialization.serialize(addressHistoryObj, AddressHistoryObj.class));
        }

        editor.apply();
    }

    public void loadAllData() {
        settings_address_history = new ArrayList<>();

        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        int size = sharedPreferences.getInt("address_history_size", 0);

        for(int i = 0; i < size; i++) {
            String serialString = sharedPreferences.getString("address_history" + i, DEFAULT);
            AddressHistoryObj addressHistoryObj = Serialization.deserialize(serialString, AddressHistoryObj.class);
            settings_address_history.add(addressHistoryObj);
        }
    }

    public void resetAllData() {
        settings_address_history = new ArrayList<>();

        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.clear();
        editor.apply();
    }

    public static String exportationVersion() {
        return "1";
    }

    public static String exportationType(String version) {
        return "!OBJECT!";
    }

    public String exportDataToJSON() throws org.json.JSONException {
        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());

        DataBridge.JSONObjectDataBridge o = new DataBridge.JSONObjectDataBridge();

        String sizeKey = "address_history_size";
        int size = sharedPreferences.getInt(sizeKey, 0);
        o.serialize(sizeKey, size, Integer.class);

        for(int i = 0; i < size; i++) {
            String key = "address_history" + i;
            String serialString = sharedPreferences.getString(key, DEFAULT);
            o.serialize(key, serialString, String.class);
        }

        return o.toStringOrNull();
    }


    public void importDataFromJSON(String s, String version) throws org.json.JSONException {
        DataBridge.JSONObjectDataBridge o = new DataBridge.JSONObjectDataBridge(s);

        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        String sizeKey = "address_history_size";
        int size = o.deserialize(sizeKey, Integer.class);
        editor.putInt(sizeKey, size);

        for(int i = 0; i < size; i++) {
            String key = "address_history" + i;
            String value = o.deserialize(key, String.class);
            editor.putString(key, Serialization.cycle(value, AddressHistoryObj.class));
        }

        editor.apply();

        // Reinitialize data.
        loadAllData();
    }
}
