package com.musicslayer.cryptobuddy.persistence;

import android.content.SharedPreferences;

import java.util.ArrayList;

import com.musicslayer.cryptobuddy.api.address.CryptoAddress;
import com.musicslayer.cryptobuddy.data.Exportation;
import com.musicslayer.cryptobuddy.json.JSONWithNull;
import com.musicslayer.cryptobuddy.data.Serialization;
import com.musicslayer.cryptobuddy.util.SharedPreferencesUtil;

public class AddressHistory implements Exportation.ExportableToJSON, Exportation.Versionable {
    // This default will cause an error when deserialized. We should never see this value used.
    public final static String DEFAULT = "null";
    final public static int HISTORY_LIMIT = 10;

    public static ArrayList<AddressHistoryObj> settings_address_history = new ArrayList<>();

    public static String getSharedPreferencesKey() {
        return "address_history_data";
    }

    public static boolean isSaved(AddressHistoryObj addressHistoryObj) {
        return settings_address_history.contains(addressHistoryObj);
    }

    public static void addAddressToHistory(AddressHistoryObj addressHistoryObj) {
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

    public static void removeAddressFromHistory(AddressHistoryObj addressHistoryObj) {
        settings_address_history.remove(addressHistoryObj);
        saveAllData();
    }

    public static void saveAllData() {
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

    public static void loadAllData() {
        settings_address_history = new ArrayList<>();

        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        int size = sharedPreferences.getInt("address_history_size", 0);

        for(int i = 0; i < size; i++) {
            String serialString = sharedPreferences.getString("address_history" + i, DEFAULT);
            AddressHistoryObj addressHistoryObj = Serialization.deserialize(serialString, AddressHistoryObj.class);
            settings_address_history.add(addressHistoryObj);
        }
    }

    public static AddressHistoryObj getFromCryptoAddress(CryptoAddress cryptoAddress) {
        for(AddressHistoryObj h : settings_address_history) {
            if(cryptoAddress.equals(h.cryptoAddress)) {
                return h;
            }
        }
        return null;
    }

    public static void resetAllData() {
        settings_address_history = new ArrayList<>();

        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.clear();
        editor.apply();
    }

    public static boolean canExport() {
        return true;
    }

    public String exportationVersion() {
        return "1";
    }

    public static String exportDataToJSON() throws org.json.JSONException {
        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());

        JSONWithNull.JSONObjectWithNull o = new JSONWithNull.JSONObjectWithNull();

        String sizeKey = "address_history_size";
        int size = sharedPreferences.getInt(sizeKey, 0);
        o.put(sizeKey, size, Integer.class);

        for(int i = 0; i < size; i++) {
            String key = "address_history" + i;
            String serialString = sharedPreferences.getString(key, DEFAULT);
            o.put(key, serialString, String.class);
        }

        return o.toStringOrNull();
    }


    public static void importDataFromJSON(String s, String version) throws org.json.JSONException {
        JSONWithNull.JSONObjectWithNull o = new JSONWithNull.JSONObjectWithNull(s);

        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        String sizeKey = "address_history_size";
        int size = o.get(sizeKey, Integer.class);
        editor.putInt(sizeKey, size);

        for(int i = 0; i < size; i++) {
            String key = "address_history" + i;
            String value = o.get(key, String.class);
            editor.putString(key, Serialization.validate(value, AddressHistoryObj.class));
        }

        editor.apply();

        // Reinitialize data.
        AddressHistory.loadAllData();
    }
}
