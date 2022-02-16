package com.musicslayer.cryptobuddy.persistence;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

import com.musicslayer.cryptobuddy.api.address.CryptoAddress;
import com.musicslayer.cryptobuddy.asset.fiatmanager.FiatManager;
import com.musicslayer.cryptobuddy.serialize.Serialization;

public class AddressHistory {
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

    public static void addAddressToHistory(Context context, AddressHistoryObj addressHistoryObj) {
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

        saveAllData(context);
    }

    public static void removeAddressFromHistory(Context context, AddressHistoryObj addressHistoryObj) {
        settings_address_history.remove(addressHistoryObj);
        saveAllData(context);
    }

    public static void saveAllData(Context context) {
        SharedPreferences settings = context.getSharedPreferences(getSharedPreferencesKey(), MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.clear();

        int size = settings_address_history.size();
        editor.putInt("address_history_size", size);

        for(int i = 0; i < size; i++) {
            AddressHistoryObj addressHistoryObj = settings_address_history.get(i);
            editor.putString("address_history" + i, Serialization.serialize(addressHistoryObj));
        }

        editor.apply();
    }

    public static void loadAllData(Context context) {
        settings_address_history = new ArrayList<>();

        SharedPreferences settings = context.getSharedPreferences(getSharedPreferencesKey(), MODE_PRIVATE);
        int size = settings.getInt("address_history_size", 0);

        for(int i = 0; i < size; i++) {
            String serialString = settings.getString("address_history" + i, DEFAULT);
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

    public static void resetAllData(Context context) {
        settings_address_history = new ArrayList<>();

        SharedPreferences settings = context.getSharedPreferences(getSharedPreferencesKey(), MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.clear();
        editor.apply();
    }

    //public String exportVersion() { return "1"; }

    public static boolean canExport() { return true; }

    public static String exportToJSON(Context context) throws org.json.JSONException {
        SharedPreferences settings = context.getSharedPreferences(getSharedPreferencesKey(), MODE_PRIVATE);

        Serialization.JSONObjectWithNull o = new Serialization.JSONObjectWithNull();

        String sizeKey = "address_history_size";
        int size = settings.getInt(sizeKey, 0);
        o.put(sizeKey, Serialization.int_serialize(size));

        for(int i = 0; i < size; i++) {
            String key = "address_history" + i;
            String serialString = settings.getString(key, DEFAULT);
            o.put(key, serialString);
        }

        return o.toStringOrNull();
    }


    public static void importFromJSON1(Context context, String s) throws org.json.JSONException {
        Serialization.JSONObjectWithNull o = new Serialization.JSONObjectWithNull(s);

        SharedPreferences settings = context.getSharedPreferences(getSharedPreferencesKey(), MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        String sizeKey = "address_history_size";
        int size = Serialization.int_deserialize(o.getString(sizeKey));
        editor.putInt(sizeKey, size);

        for(int i = 0; i < size; i++) {
            String key = "address_history" + i;
            String value = o.getString(key);
            editor.putString(key, Serialization.validate(value, AddressHistoryObj.class));
        }

        editor.apply();

        // Reinitialize data.
        AddressHistory.loadAllData(context);
    }
}
