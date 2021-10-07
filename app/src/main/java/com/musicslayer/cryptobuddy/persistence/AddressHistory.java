package com.musicslayer.cryptobuddy.persistence;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.HashMap;

import static android.content.Context.MODE_PRIVATE;

import com.musicslayer.cryptobuddy.util.ThrowableUtil;
import com.musicslayer.cryptobuddy.serialize.Serialization;

public class AddressHistory {
    // This default will cause an error when deserialized. We should never see this value used.
    public final static String DEFAULT = "null";

    final public static int HISTORY_LIMIT = 10;

    // Store the raw strings too in case we need them in a data dump.
    // Once everything has successfully loaded we stop updating these.
    public static HashMap<Integer, String> settings_address_history_raw = new HashMap<>();

    public static ArrayList<AddressHistoryObj> settings_address_history = new ArrayList<>();

    public static boolean isSaved(AddressHistoryObj addressHistoryObj) {
        return settings_address_history.contains(addressHistoryObj);
    }

    public static void addAddress(Context context, AddressHistoryObj addressHistoryObj) {
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

    public static void removeAddress(Context context, AddressHistoryObj addressHistoryObj) {
        settings_address_history.remove(addressHistoryObj);
        saveAllData(context);
    }

    public static void saveAllData(Context context) {
        SharedPreferences settings = context.getSharedPreferences("address_history_data", MODE_PRIVATE);
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
        settings_address_history_raw = new HashMap<>();
        settings_address_history = new ArrayList<>();

        SharedPreferences settings = context.getSharedPreferences("address_history_data", MODE_PRIVATE);
        int size = settings.getInt("address_history_size", 0);

        settings_address_history_raw.put(-1, Integer.toString(size));

        for(int i = 0; i < size; i++) {
            String serialString = settings.getString("address_history" + i, DEFAULT);
            settings_address_history_raw.put(i, serialString == null ? "null" : serialString);

            AddressHistoryObj addressHistoryObj = Serialization.deserialize(serialString, AddressHistoryObj.class);
            settings_address_history.add(addressHistoryObj);
        }

        saveAllData(context);
    }

    public static HashMap<String, String> getAllData() {
        HashMap<String, String> hashMap = new HashMap<>();

        for(int key : settings_address_history_raw.keySet()) {
            if(key == -1) {
                hashMap.put("SIZE", settings_address_history_raw.get(key));
            }
            else {
                hashMap.put("RAW" + key, settings_address_history_raw.get(key));
            }
        }

        // We want the raw data even if this next piece errors.
        try {
            for(int i = 0; i < settings_address_history.size(); i++) {
                AddressHistoryObj addressHistoryObj = settings_address_history.get(i);
                hashMap.put("OBJ" + i, Serialization.serialize(addressHistoryObj));
            }
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
        }

        return hashMap;
    }

    public static void resetAllData(Context context) {
        settings_address_history = new ArrayList<>();

        SharedPreferences settings = context.getSharedPreferences("address_history_data", MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.clear();
        editor.apply();
    }
}
