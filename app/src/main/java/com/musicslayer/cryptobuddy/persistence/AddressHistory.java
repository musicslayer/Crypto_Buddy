package com.musicslayer.cryptobuddy.persistence;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.HashMap;

import static android.content.Context.MODE_PRIVATE;

import com.musicslayer.cryptobuddy.util.Serialization;

public class AddressHistory {
    final public static int HISTORYLIMIT = 10;
    public static ArrayList<AddressHistoryObj> settings_address_history = new ArrayList<>();

    public static boolean isSaved(AddressHistoryObj addressHistoryObj) {
        return settings_address_history.contains(addressHistoryObj);
    }

    public static void addAddress(Context context, AddressHistoryObj addressHistoryObj) {
        if(isSaved(addressHistoryObj)) {
            settings_address_history.remove(addressHistoryObj);
            settings_address_history.add(0, addressHistoryObj);
        }
        else if(settings_address_history.size() == AddressHistory.HISTORYLIMIT) {
            settings_address_history.remove(AddressHistory.HISTORYLIMIT - 1);
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
        settings_address_history = new ArrayList<>();

        SharedPreferences settings = context.getSharedPreferences("address_history_data", MODE_PRIVATE);
        int size = settings.getInt("address_history_size", 0);

        for(int i = 0; i < size; i++) {
            String serialString = settings.getString("address_history" + i, "");
            AddressHistoryObj addressHistoryObj = Serialization.deserialize(serialString, AddressHistoryObj.class);

            // If there is any problem at all, don't add this one.
            if(addressHistoryObj != null) {
                settings_address_history.add(addressHistoryObj);
            }
        }

        saveAllData(context);
    }

    public static void resetAllData(Context context) {
        settings_address_history = new ArrayList<>();

        SharedPreferences settings = context.getSharedPreferences("address_history_data", MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.clear();
        editor.putInt("address_history_size", 0);
        editor.apply();
    }

    public static HashMap<String, String> getDataDump() {
        HashMap<String, String> hashMap = new HashMap<>();
        for(int i = 0; i < settings_address_history.size(); i++) {
            AddressHistoryObj addressHistoryObj = settings_address_history.get(i);
            hashMap.put(Integer.toString(i), Serialization.serialize(addressHistoryObj));
        }
        return hashMap;
    }
}
